package webscraping.lemmasservice.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import webscraping.lemmasservice.model.Index;
import webscraping.lemmasservice.model.Lemma;
import webscraping.lemmasservice.repository.LemmaRepository;
import webscraping.lemmasservice.util.BeanUtils;
import webscraping.lemmasservice.util.LemmasParser;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LemmaService {

    private final RedisLemmaService redisLemmaService;
    private final LemmaRepository lemmaRepository;

    @Transactional
    public void saveAll(List<Lemma> lemmaList) {
        List<String> lemmaKeys = Collections.unmodifiableList(
                lemmaList.stream().map(Lemma::getLemma).collect(Collectors.toList())
        );

        Map<String, Lemma> cachedLemmas = redisLemmaService.getLemmaFromCache(lemmaKeys);

        Set<String> keysToFetchFromDB = Collections.synchronizedSet(new HashSet<>(lemmaKeys));
        keysToFetchFromDB.removeAll(cachedLemmas.keySet());

        final Map<String, Lemma> dbLemmas = new ConcurrentHashMap<>();

        if (!keysToFetchFromDB.isEmpty()) {
            synchronized (dbLemmas) {
                List<String> missingKeys = new ArrayList<>(keysToFetchFromDB);
                missingKeys.forEach(lemmaKey -> {
                    Lemma lemma = lemmaRepository.findByLemma(lemmaKey);
                    dbLemmas.put(lemmaKey, lemma);
                });
            }
        }

        lemmaList.forEach(lemma -> {
            Lemma existLemma = cachedLemmas.get(lemma.getLemma());
            if (existLemma == null) {
                existLemma = dbLemmas.get(lemma.getLemma());
            }

            if (existLemma != null) {
                lemma.setId(existLemma.getId());
                lemma.setFrequency(existLemma.getFrequency() + 1);

                List<Index> indexes = new ArrayList<>(existLemma.getIndexes());
                indexes.addAll(lemma.getIndexes());
                lemma.setIndexes(indexes);

                update(lemma);
            } else {
                save(lemma);

                redisLemmaService.saveLemmaToCache(lemma.getLemma(), lemma);
            }
        });
    }

    public Lemma save(Lemma lemma) {
        return lemmaRepository.save(lemma);
    }

    public Lemma update(Lemma lemma) {
        Lemma oldLemma = findById(lemma.getId());
        BeanUtils.copyNotNullProperties(lemma, oldLemma);
        return lemmaRepository.save(oldLemma);
    }

    public Lemma findById(Long id) {
        return lemmaRepository.findById(id).orElse(null);
    }

    @SneakyThrows
    public List<UUID> findBlogIdsByRelevance(String query, int topN) {
        return findBlogIdsByRelevance(query, topN, -1L);
    }

    @SneakyThrows
    public List<UUID> findBlogIdsByRelevance(String query, int topN, Long siteId) {
        // Получаем хеш-карту лемм из запроса
        Map<String, Integer> queryLemmas = LemmasParser.getLemmasHashMap(query);

        // Сохраняем мапу для подсчета релевантности по pageId (UUID)
        Map<UUID, Float> pageRelevanceMap = new HashMap<>();

        // Получаем все леммы из репозитория для всех лемм из запроса, фильтруем по siteId
        List<Lemma> lemmaList;
        if (siteId == -1L) {
            lemmaList = lemmaRepository.findByLemmaIn(new ArrayList<>(queryLemmas.keySet()));
        } else {
            lemmaList = lemmaRepository.findByLemmaInAndSiteId(new ArrayList<>(queryLemmas.keySet()), siteId);
        }

        // Создаем мапу для быстрого поиска лемм по ключу
        Map<String, Lemma> lemmaMap = lemmaList.stream()
                .collect(Collectors.toMap(Lemma::getLemma, lemma -> lemma));

        // Проходим по всем леммам из запроса
        for (String lemmaKey : queryLemmas.keySet()) {
            Lemma lemma = lemmaMap.get(lemmaKey);

            if (lemma != null) {
                // Для каждой леммы перебираем индексы
                for (Index index : lemma.getIndexes()) {
                    // Считаем релевантность для каждого индекса
                    float relevance = calculateRelevance(index, queryLemmas);

                    // Накапливаем релевантность для каждого pageId (UUID)
                    pageRelevanceMap.merge(index.getPageId(), relevance, Float::sum);
                }
            }
        }

        // Сортируем страницы по релевантности и выбираем топ N ID с наивысшей релевантностью
        return pageRelevanceMap.entrySet().stream()
                .sorted(Map.Entry.<UUID, Float>comparingByValue().reversed()) // Сортируем по убыванию релевантности
                .limit(topN) // Ограничиваем количеством топовых результатов
                .map(Map.Entry::getKey) // Извлекаем pageId (UUID)
                .collect(Collectors.toList()); // Собираем в список
    }


    private float calculateRelevance(Index index, Map<String, Integer> queryLemmas) {
        float relevance = 0;

        // Проходим по всем леммам из запроса
        for (String lemmaKey : queryLemmas.keySet()) {
            // Находим лемму в базе данных по ключу
            Lemma lemma = lemmaRepository.findByLemma(lemmaKey);

            if (lemma != null) {
                // Учитываем частоту леммы в запросе
                int queryLemmaFrequency = queryLemmas.get(lemmaKey);
                int pageLemmaFrequency = lemma.getFrequency(); // Частота леммы на странице
                relevance += pageLemmaFrequency * queryLemmaFrequency * 0.5f; // Умножаем на частоту из запроса

                // Учитываем рейтинг леммы на странице
                Integer indexRank = index.getRank(); // Рейтинг индекса леммы на странице
                if (indexRank != null) {
                    relevance += indexRank * 0.3f; // Умножаем на рейтинг индекса
                }
            }
        }

        return relevance;
    }

    public void deleteAllBySiteId(Long siteId) {
        lemmaRepository.deleteAllBySiteId(siteId);
    }

}
