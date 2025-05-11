package webscraping.lemmasservice.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import webscraping.lemmasservice.dto.BlogDto;
import webscraping.lemmasservice.model.Index;
import webscraping.lemmasservice.model.Lemma;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class LemmasParser {

    public static List<Lemma> lemmaParse(BlogDto blog) {
        List<Lemma> result = new ArrayList<>();
        try {
            HashMap<String, Integer> lemmas = getLemmasHashMap(blog.getBlogText());

            for (HashMap.Entry<String, Integer> entry : lemmas.entrySet()) {
                Lemma lemma = new Lemma();
                Index index = new Index();
                index.setPageId(blog.getId());
                index.setRank(entry.getValue());
                lemma.setLemma(entry.getKey());
                lemma.setFrequency(1);
                lemma.setSiteId(blog.getSiteId());
                List<Index> indexes = new ArrayList<>();
                indexes.add(index);
                lemma.setIndexes(indexes);
                index.setLemma(lemma);
                result.add(lemma);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return result;
    }

    public static HashMap<String, Integer> getLemmasHashMap(String text) throws IOException {
        long start = System.currentTimeMillis();
        log.info("Starting get LemmasHashMap");

        String[] wordsArray = text.split("\\s+|,\\s*|\\.\\s*|;\\s*");

        List<String> wordsList = new ArrayList<>(Arrays.asList(wordsArray));

        ConcurrentHashMap<String, Integer> lemmas = new ConcurrentHashMap<>();
        LuceneMorphology russianMorphology = new RussianLuceneMorphology();
        LuceneMorphology englishMorphology = new EnglishLuceneMorphology();

        for (String word : wordsList) {
            LuceneMorphology luceneMorph;
            if (word.matches("[А-Яа-я]+")) {
                luceneMorph = russianMorphology;
            } else if (word.matches("[A-Za-z]+")) {
                luceneMorph = englishMorphology;
            } else {
                continue;
            }
            List<String> wordBaseForms = luceneMorph.getNormalForms(word.toLowerCase(Locale.ROOT));
            List<String> wordBaseInfo = luceneMorph.getMorphInfo(word.toLowerCase(Locale.ROOT));

            wordBaseForms.forEach(baseForm -> {
                if (wordBaseInfo.get(0).contains("|l") || wordBaseInfo.get(0).contains("|n") || wordBaseInfo.get(0).contains("|o")) {
                    return;
                }
                if (lemmas.containsKey(baseForm)) {
                    lemmas.put(baseForm, lemmas.get(baseForm) + 1);
                } else {
                    lemmas.put(baseForm, 1);
                }
            });
        }

        log.info("Finishing get LemmasHashMap: {}ms", System.currentTimeMillis() - start);
        return new HashMap<>(lemmas);
    }
}
