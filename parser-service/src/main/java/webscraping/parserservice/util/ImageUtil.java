package webscraping.parserservice.util;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class ImageUtil {
    private static final Pattern IMG_URL_PATTERN = Pattern.compile(
            "(?i)(https?://[^\\s'\"]+\\.(webp|png|jpe?g|gif|bmp))(?:\\?[^\\s'\"]+)?"
    );

    public static String extractProductImageUrl(Element productContainer) {
        // 1. Ищем главное изображение по специфичным классам
        Element mainImage = findMainImage(productContainer);
        if (mainImage != null) {
            String url = extractFromImgElement(mainImage);
            if (!"Empty".equals(url)) return url;
        }

        // 2. Ищем в галереях
        String galleryImage = findInGalleries(productContainer);
        if (!"Empty".equals(galleryImage)) return galleryImage;

        // 3. Ищем фоновые изображения
        String bgImage = extractBackgroundImage(productContainer);
        if (!"Empty".equals(bgImage)) return bgImage;

        // 4. Fallback: первое подходящее изображение в контейнере
        return findFirstValidImage(productContainer);
    }

    private static Element findMainImage(Element container) {
        // Приоритетные селекторы для главного изображения
        String[] mainImageSelectors = {
                "img[itemprop=image]",
                ".main-image img",
                ".product-image img",
                ".thumb-img",
                ".zoomImg",
                "img[class*=-main]",
                "img[class*=-large]"
        };

        for (String selector : mainImageSelectors) {
            Element img = container.selectFirst(selector);
            if (img != null) return img;
        }
        return null;
    }

    private static String findInGalleries(Element container) {
        Elements galleries = container.select(".gallery, .slider, .carousel");
        for (Element gallery : galleries) {
            String url = extractFromImgElement(gallery.selectFirst("img"));
            if (!"Empty".equals(url)) return url;
        }
        return "Empty";
    }

    private static String findFirstValidImage(Element container) {
        for (Element img : container.select("img")) {
            String url = extractFromImgElement(img);
            if (!"Empty".equals(url)) return url;
        }
        return "Empty";
    }

    // Остальные методы без изменений
    private static String extractFromImgElement(Element img) {
        if (img == null) return "Empty";

        String[] attrPriority = {"data-src", "src", "data-original", "data-srcset", "srcset"};
        for (String attr : attrPriority) {
            if (img.hasAttr(attr)) {
                String url = processSourceValue(img.absUrl(attr));
                if (isValidImageUrl(url)) return url;
            }
        }
        return "Empty";
    }

    private static String extractBackgroundImage(Element container) {
        String style = container.attr("style");
        Matcher matcher = Pattern.compile(
                "url\\(['\"]?(https?://[^\\s'\"]+\\.(webp|png|jpe?g|gif|bmp))['\"]?\\)"
        ).matcher(style);
        return matcher.find() ? matcher.group(1) : "Empty";
    }


    private static String processSourceValue(String value) {
        if (value.contains(",")) {
            return value.split(",")[0].replaceAll("\\s+\\d+[wx]", "").trim();
        }
        return value.trim();
    }

    private static boolean isValidImageUrl(String url) {
        return url != null && !url.isEmpty() &&
                !url.startsWith("data:image") &&
                IMG_URL_PATTERN.matcher(url).matches();
    }


    public static List<String> extractImageUrlsFromHtmlText(String html, String baseUrl) {
        Set<String> imageUrls = new LinkedHashSet<>(); // Используем Set для автоматического удаления дубликатов

        // 1. Ищем абсолютные URL изображений
        Pattern absolutePattern = Pattern.compile(
                "(https?://[^\\s'\"]+\\.(?:webp|png|jpe?g|gif|bmp))(?:\\?[^\\s'\"]+)?",
                Pattern.CASE_INSENSITIVE
        );

        Matcher absoluteMatcher = absolutePattern.matcher(html);
        while (absoluteMatcher.find()) {
            String url = absoluteMatcher.group(1);
            if (!url.startsWith("data:image")) {
                imageUrls.add(url);
            }
        }

        // 2. Ищем относительные пути (/path/to/image.jpg)
        Pattern relativePattern = Pattern.compile(
                "[\"'](/[^\\s'\"]+\\.(?:webp|png|jpe?g|gif|bmp))(?:\\?[^\\s'\"]+)?[\"']",
                Pattern.CASE_INSENSITIVE
        );

        Matcher relativeMatcher = relativePattern.matcher(html);
        while (relativeMatcher.find()) {
            String relativePath = relativeMatcher.group(1);
            imageUrls.add(combineUrls(baseUrl, relativePath));
        }

        // 3. Ищем протоколо-относительные URL (//example.com/image.jpg)
        Pattern protocolRelativePattern = Pattern.compile(
                "[\"'](//[^\\s'\"]+\\.(?:webp|png|jpe?g|gif|bmp))(?:\\?[^\\s'\"]+)?[\"']",
                Pattern.CASE_INSENSITIVE
        );

        Matcher protocolRelativeMatcher = protocolRelativePattern.matcher(html);
        while (protocolRelativeMatcher.find()) {
            String url = protocolRelativeMatcher.group(1);
            imageUrls.add("https:" + url); // Добавляем https: для протоколо-относительных URL
        }

        return new ArrayList<>(imageUrls);
    }

    // Вспомогательный метод для объединения базового URL и относительного пути
    private static String combineUrls(String baseUrl, String relativePath) {
        if (baseUrl.endsWith("/") && relativePath.startsWith("/")) {
            return baseUrl + relativePath.substring(1);
        } else if (!baseUrl.endsWith("/") && !relativePath.startsWith("/")) {
            return baseUrl + "/" + relativePath;
        } else {
            return baseUrl + relativePath;
        }
    }
}