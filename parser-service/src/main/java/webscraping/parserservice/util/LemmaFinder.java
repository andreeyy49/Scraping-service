package webscraping.parserservice.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class LemmaFinder {

    public static HashMap<String, Integer> getLemmas(String html) {
        String[] wordsArray = Jsoup.parse(html).text().split("\\s+|,\\s*|\\.\\s*|;\\s*");

        List<String> wordsList = new ArrayList<>(Arrays.asList(wordsArray));

        ConcurrentHashMap<String, Integer> lemmas = new ConcurrentHashMap<>();
        try {
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new HashMap<>(lemmas);
    }
}
