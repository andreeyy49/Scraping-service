package webscraping.lemmasservice.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import webscraping.lemmasservice.model.Index;
import webscraping.lemmasservice.model.Lemma;
import webscraping.lemmasservice.repository.IndexRepository;
import webscraping.lemmasservice.repository.LemmaRepository;
import webscraping.lemmasservice.util.LemmasParser;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class LemmaService {

    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    @Transactional
    public void saveAll(List<Lemma> lemmaList) {
        if (lemmaList == null || lemmaList.isEmpty()) {
            return;
        }

        // Группируем и сортируем леммы по ключу для детерминированного порядка
        Map<String, List<Lemma>> groupedLemmas = lemmaList.stream()
                .filter(lemma -> lemma != null && lemma.getLemma() != null && lemma.getSiteId() != null)
                .collect(Collectors.groupingBy(
                        lemma -> lemma.getSiteId() + "::" + lemma.getLemma(),
                        TreeMap::new, // Используем TreeMap для автоматической сортировки
                        Collectors.toList()
                ));

        // Сначала обновляем частотность в отсортированном порядке
        groupedLemmas.keySet().stream().sorted().forEach(key -> {
            String[] parts = key.split("::", 2);
            Long siteId = Long.parseLong(parts[0]);
            String lemmaValue = parts[1];
            lemmaRepository.upsertLemma(siteId, lemmaValue);
        });

        // Затем обрабатываем индексы в том же порядке
        groupedLemmas.keySet().stream().sorted().forEach(key -> {
            String[] parts = key.split("::", 2);
            Long siteId = Long.parseLong(parts[0]);
            String lemmaValue = parts[1];

            Lemma lemma = lemmaRepository.findByLemmaAndSiteId(lemmaValue, siteId)
                    .orElseThrow(() -> new RuntimeException("Lemma should exist after upsert"));

            List<Index> allIndexes = groupedLemmas.get(key).stream()
                    .flatMap(l -> l.getIndexes() != null ? l.getIndexes().stream() : Stream.empty())
                    .peek(index -> index.setLemma(lemma))
                    .collect(Collectors.toList());

            if (!allIndexes.isEmpty()) {
                indexRepository.saveAll(allIndexes);
            }
        });
    }

    @SneakyThrows
    public List<UUID> findBlogIdsByRelevance(String query, int topN) {
        return findBlogIdsByRelevance(query, topN, -1L);
    }

    @SneakyThrows
    public List<UUID> findBlogIdsByRelevance(String query, int topN, Long siteId) {
        // Проверка входных параметров
        if (query == null || query.trim().isEmpty() || topN <= 0) {
            return Collections.emptyList();
        }

        // Получаем леммы из запроса
        Map<String, Integer> queryLemmas = LemmasParser.getLemmasHashMap(query);
        if (queryLemmas.isEmpty()) {
            return Collections.emptyList();
        }

        // Загружаем все нужные леммы и их индексы
        List<Lemma> lemmaList;
        if (siteId == -1L) {
            lemmaList = lemmaRepository.findByLemmaInWithIndexes(new ArrayList<>(queryLemmas.keySet()));
        } else {
            lemmaList = lemmaRepository.findByLemmaInAndSiteIdWithIndexes(
                    new ArrayList<>(queryLemmas.keySet()), siteId);
        }

        // Логирование для отладки
        log.debug("Found {} lemmas for query: {}", lemmaList.size(), query);

        // Подсчет релевантности
        Map<UUID, Float> pageRelevanceMap = new HashMap<>();

        for (Lemma lemma : lemmaList) {
            if (lemma.getIndexes() == null || lemma.getIndexes().isEmpty()) {
                continue;
            }

            for (Index index : lemma.getIndexes()) {
                float relevance = calculateRelevance(index, queryLemmas, lemma);
                pageRelevanceMap.merge(index.getPageId(), relevance, Float::sum);
            }
        }

        // Сортировка и возврат результатов
        return pageRelevanceMap.entrySet().stream()
                .sorted(Map.Entry.<UUID, Float>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }


    private float calculateRelevance(Index index, Map<String, Integer> queryLemmas, Lemma lemma) {
        float relevance = 0f;

        // Вес частоты леммы в запросе
        int queryFrequency = queryLemmas.getOrDefault(lemma.getLemma(), 1);
        relevance += queryFrequency * 0.7f;

        // Вес частоты леммы в индексе
        relevance += lemma.getFrequency() * 0.5f;

        // Вес ранга индекса
        if (index.getRank() != null) {
            relevance += index.getRank() * 0.3f;
        }

        return relevance;
    }
}
