package webscraping.urlanalyzerservice.util;

import java.util.HashMap;
import java.util.Map;

public class HeadersBuilder {

    public static Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
        headers.put("Connection", "keep-alive");
        headers.put("Referer", "https://www.google.com");
        headers.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");

        return headers;
    }
}
