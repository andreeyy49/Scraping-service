package webscraping.urlanalyzerservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import webscraping.urlanalyzerservice.client.PlaywrightClient;
import webscraping.urlanalyzerservice.enums.ParserType;
import webscraping.urlanalyzerservice.exception.ConnectionException;
import webscraping.urlanalyzerservice.model.SiteStructure;
import webscraping.urlanalyzerservice.util.HeadersBuilder;
import webscraping.urlanalyzerservice.util.KeyWords;
import webscraping.urlanalyzerservice.enums.SiteDataType;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlAnalyzeService {

    private final PlaywrightClient playwrightClient;

    public SiteDataType analyze(String url) {
        url = formattedUrl(url);
        log.info("Analysing URL: " + url);
        try {
            SiteStructure site = getConnection(url);

            if (!site.getSiteDataType().equals(SiteDataType.UN_TYPE)) {
                return site.getSiteDataType();
            }

            String title = site.getTitle();
            String text = site.getBody();

            List<String> eCommerceKeyWords = KeyWords.getECommerceKeyWords();
            List<String> blogKeyWords = KeyWords.getBlogKeyWords();

            int coincidenceECommerce = 0;
            int coincidenceBlog = 0;

            for (String key : eCommerceKeyWords) {
                if (title.contains(key)) {
                    log.info("title contains {}", key);
                    return SiteDataType.E_COMMERCE;
                }
                if (text.contains(key)) {
                    coincidenceECommerce++;
                }
            }

            for (String key : blogKeyWords) {
                if (title.contains(key)) {
                    log.info("title contains {}", key);
                    return SiteDataType.BLOG;
                }
                if (text.contains(key)) {
                    coincidenceBlog++;
                }
            }

            double eCommercePercent = (double) 100 / eCommerceKeyWords.size() * coincidenceECommerce;
            double blogPercent = (double) 100 / blogKeyWords.size() * coincidenceBlog;

            log.info("blog percent: {}, e-commerce percent: {}", blogPercent, eCommercePercent);

            return eCommercePercent > blogPercent
                    ? SiteDataType.E_COMMERCE : SiteDataType.BLOG;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SiteStructure getConnection(String url) throws IOException {
        SiteStructure siteStructure = new SiteStructure();
        if(getParserType(url).equals(ParserType.JSOUP)) {
            try {
                Document document = Jsoup
                        .connect(url)
                        .headers(HeadersBuilder.getHeaders())
                        .get();

                siteStructure.setTitle(document.title());
                if (siteStructure.getTitle().isEmpty()) {
                    siteStructure.setTitle(document.select("meta[property=og:title]").attr("content"));
                }
                siteStructure.setBody(document.body().text());
                siteStructure.setSiteDataType(SiteDataType.UN_TYPE);
            } catch (IOException e) {
                throw new ConnectException("Ошибка парсинга jsoup url: " + url);
            }
        } else {
            try {
                siteStructure.setTitle(playwrightClient.getTitle(url));
                siteStructure.setSiteDataType(analyzeTitle(siteStructure.getTitle()));

                if (siteStructure.getSiteDataType().equals(SiteDataType.UN_TYPE)) {
                    siteStructure.setBody(playwrightClient.getText(url));
                }
            } catch (Exception e) {
                throw new ConnectException("Ошибка парсинга playwright url: " + url);
            }

            return siteStructure;
        }

        log.info("Site title is empty: {}, body is empty: {}",
                siteStructure.getTitle().isEmpty(),
                siteStructure.getBody().isEmpty());
        return siteStructure;
    }

    private String formattedUrl(String url) {
        String[] tempArray = url.split("/");
        return "http://" + tempArray[2] + "/";
    }

    private SiteDataType analyzeTitle(String title) {
        List<String> eCommerceKeyWords = KeyWords.getECommerceKeyWords();
        List<String> blogKeyWords = KeyWords.getBlogKeyWords();

        for (String key : eCommerceKeyWords) {
            if (title.contains(key)) {
                log.info("title contains {}", key);
                return SiteDataType.E_COMMERCE;
            }
        }

        for (String key : blogKeyWords) {
            if (title.contains(key)) {
                log.info("title contains {}", key);
                return SiteDataType.BLOG;
            }
        }

        return SiteDataType.UN_TYPE;
    }

    public ParserType getParserType(String url) {
        try {
            Document document = Jsoup
                    .connect(url)
                    .headers(HeadersBuilder.getHeaders())
                    .get();

            if (document.text().isEmpty()) {
                return ParserType.PLAYWRIGHT;
            }

            return ParserType.JSOUP;
        } catch (IOException e) {
            String title = playwrightClient.getTitle(url);
            if(title.isEmpty()) {
                throw new ConnectionException("Ошибка парсинга url: " + url);
            }
            return ParserType.PLAYWRIGHT;
        }
    }
}
