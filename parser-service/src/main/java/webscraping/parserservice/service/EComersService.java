package webscraping.parserservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import webscraping.parserservice.dto.S3KeyDto;
import webscraping.parserservice.model.Product;
import webscraping.parserservice.util.ImageUtil;
import webscraping.parserservice.util.S3KeyParser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class EComersService {

    private Product findProductData(String html) {
        Document document = Jsoup.parse(html);
        Elements elements = document.select("div, section, article"); // Контейнеры

        for (Element container : elements) {
            Element titleElement = container.selectFirst("h1, h2, h3, [class*=title i]");
            Element imgElement = container.selectFirst(
                    "[src*=''], [data-src*=''], img, image, meta[property*=image], [class*=img i], [class*=image i]"
            );

            if (titleElement != null && imgElement != null) {
                // Используем новый универсальный метод для поиска цены
                String price = extractPriceUniversal(container);
                if (price == null) continue;

                String title = titleElement.text();
                String imageUrl = ImageUtil.extractProductImageUrl(container);
                if ("Empty".equals(imageUrl)) continue;

                Product product = new Product();
                product.setTitle(title);
                product.setCost(price);
                product.setImages(List.of(imageUrl));
                return product;
            }
        }
        return null;
    }

    // Остальные методы остаются без изменений
    public Product buildProduct(String path, String html, String s3key) {
        Product product = findProductData(html);

        if (product == null) {
            log.warn("Product is: {} is empty", path);
            return new Product();
        }

        S3KeyDto s3KeyDto = S3KeyParser.getS3KeyDto(s3key);
        product.setId(UUID.randomUUID());
        product.setPath(path);
        product.setParseTime(s3KeyDto.getDate());
        product.setSiteId(s3KeyDto.getSiteId());

        return product;
    }

    private String extractPriceUniversal(Element container) {
        // 1. Ищем элементы с ценой по классу/id (регистронезависимо)
        Elements priceElements = container.select(
                "[class*='price' i], [class*='cost' i], [id*='price' i], [id*='cost' i]"
        );

        // 2. Добавляем элементы с микроразметкой
        priceElements.addAll(container.select("[itemprop=price]"));

        // 3. Добавляем элементы с символами валют
        priceElements.addAll(container.select(
                ":containsOwn(₽), :containsOwn(руб), :containsOwn($), :containsOwn(€)"
        ));

        // 4. Проверяем все кандидаты
        for (Element el : priceElements) {
            String price = extractPriceFromElement(el);
            if (price != null) {
                return price;
            }
        }

        // 5. Последняя попытка: поиск чисел рядом с валютами
        Elements currencyElements = container.select(
                ":matchesOwn(\\d+\\s*[₽$€£руб])"
        );
        for (Element el : currencyElements) {
            String price = extractPriceFromText(el.text());
            if (isValidPrice(price)) {
                return price;
            }
        }

        return null;
    }

    private String extractPriceFromElement(Element el) {
        // Проверяем атрибут content (для микроразметки)
        if (el.hasAttr("content")) {
            String price = extractPriceFromText(el.attr("content"));
            if (isValidPrice(price)) {
                return price;
            }
        }

        // Проверяем текст элемента
        String price = extractPriceFromText(el.text());
        if (isValidPrice(price)) {
            return price;
        }

        // Проверяем data-атрибуты
        for (org.jsoup.nodes.Attribute attr : el.attributes()) {
            if (attr.getKey().startsWith("data-") &&
                    (attr.getKey().contains("price") || attr.getKey().contains("cost"))) {
                price = extractPriceFromText(attr.getValue());
                if (isValidPrice(price)) {
                    return price;
                }
            }
        }

        return null;
    }

    private String extractPriceFromText(String text) {
        List<String> skipWords = List.of("рассрочка", "от", "доступно", "платёж", "в месяц", "кредит");

        // Удаляем лишние символы, кроме цифр, пробелов, точек и запятых
        String cleaned = text.replaceAll("[^\\d.,\\s]", " ").trim();

        // Соединяем пробелы между цифрами (например, 12 345 → 12345)
        cleaned = cleaned.replaceAll("(\\d)\\s+(\\d)", "$1$2");

        // Проверка на нежелательные слова
        String lower = text.toLowerCase();
        for (String skipWord : skipWords) {
            if (lower.contains(skipWord)) {
                return null;
            }
        }

        // Ищем числа
        Matcher matcher = Pattern.compile("(\\d+[.,\\s]?\\d*[.,\\s]?\\d*)").matcher(cleaned);
        List<String> prices = new ArrayList<>();

        while (matcher.find()) {
            String rawPrice = matcher.group(1).replaceAll("\\s", "");
            String normalizedPrice = normalizePrice(rawPrice);
            if (isValidPrice(normalizedPrice)) {
                prices.add(normalizedPrice);
            }
        }

        return prices.stream()
                .filter(this::isValidPrice)
                .max(Comparator.comparingDouble(Double::parseDouble))
                .orElse(null);
    }

    private String normalizePrice(String price) {
        // Пример: 12.345,67 или 12,345.67 → 12345.67
        if (price.matches(".*[.,]\\d{2}$")) {
            // Последние 2 цифры после , или . — это копейки
            if (price.contains(",") && price.contains(".")) {
                if (price.lastIndexOf(",") > price.lastIndexOf(".")) {
                    price = price.replace(".", "").replace(",", ".");
                } else {
                    price = price.replace(",", "");
                }
            } else if (price.contains(",")) {
                price = price.replace(",", ".");
            } else {
                price = price.replace(".", ".");
            }
        } else {
            // Нет копеек — просто убираем все разделители
            price = price.replaceAll("[.,]", "");
        }
        return price;
    }


    private boolean isValidPrice(String price) {
        if (price == null || price.isEmpty()) {
            return false;
        }

        try {
            double value = Double.parseDouble(price);
            return value > 10 && value < 1000000;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}