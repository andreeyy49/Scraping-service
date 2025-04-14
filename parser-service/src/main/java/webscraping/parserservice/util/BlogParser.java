package webscraping.parserservice.util;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

@Slf4j
public class BlogParser {

    public static List<String> findKeyWord(String html) {
        List<String> keyWords = new ArrayList<>(getTags(html));
        String content = getContent(html);

        HashMap<String, Integer> lemmas = LemmaFinder.getLemmas(content);
        keyWords.addAll(lemmas.entrySet()
                .stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .toList());

        return new HashSet<>(keyWords).stream().toList();
    }

    public static String getContent(String html) {
        Document doc = Jsoup.parse(html);

        // Проверяем разные теги, учитываем структуры разных сайтов
        Element article = getBlogContainer(html);

        if (article != null) {
            String content = article.text();
            // Проверяем, что контент достаточно длинный
            if (content.length() >= 300) {  // фильтруем страницы с малым содержанием
                return content;
            }
        }

        // Если не нашли стандартные теги, берем первый длинный <p>
        return doc.select("p").stream()
                .map(Element::text)
                .filter(p -> p.length() > 300)  // фильтруем короткие <p>
                .findFirst()
                .orElse(null);
    }

    private static Element getBlogContainer(String html) {
        Document doc = Jsoup.parse(html);

        // Проверяем разные теги, учитываем структуры разных сайтов
        return doc.selectFirst(
                "article, section.article-content, div.article-content, div.post-content, " +
                        "main, div.main-content, div.content, div.entry-content, " +
                        "div.blog-post, div.news-text"
        );
    }


    private static List<String> getTags(String html) {
        List<String> keywordsList = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        // Основные селекторы тегов
        Elements tagElements = doc.select(
                "section.article-tags, footer, .article-footer, .post-footer, .tags, .keywords, " +
                        "meta[name=keywords], meta[property=article:tag]"
        );

        for (Element tagElement : tagElements) {
            if (tagElement.tagName().equals("meta")) {
                String content = tagElement.attr("content");
                if (!content.isEmpty()) {
                    keywordsList.addAll(Arrays.asList(content.split(",")));
                }
            } else {
                tagElement.select("a, span").forEach(tag -> keywordsList.add(tag.text()));
            }
        }

        return keywordsList.stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public static List<String> getImagesFromArticle(String html, String baseUrl) {
        Document doc = Jsoup.parse(html);

        Element imgElement = doc.selectFirst(
                "[src*=''], [data-src*=''], img, image, meta[property*=image], [class*=img], [class*=image]"
        );

        if (imgElement == null) {
            return List.of("Empty");
        }
        List<String> result = ImageUtil.extractImageUrlsFromHtmlText(html, baseUrl);

        if (result.isEmpty()) return List.of("Empty");

        return result;
    }

    public static String getBaseUrl(String path) {
        List<String> parts = Arrays.stream(path.split("/")).toList();
        return parts.get(0) + "//" + parts.get(2);
    }
}