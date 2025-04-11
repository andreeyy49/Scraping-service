package webscraping.crawlerservice.worker;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import webscraping.crawlerservice.client.PlaywrightClient;
import webscraping.crawlerservice.enums.ParserType;
import webscraping.crawlerservice.model.PageUrl;
import webscraping.crawlerservice.model.Site;
import webscraping.crawlerservice.util.WorkerStarter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class PagesUrlSummer extends RecursiveTask<List<PageUrl>> {
    private final PageUrl address;
    private final String headAddress;
    private final PlaywrightClient playwrightClient;
    private static String token;
    private final ParserType parserType;
    private final Site parentSite;
    private static final HttpClient client = HttpClient.newHttpClient();

    // Глобальный набор для отслеживания обработанных URL
    private static Set<String> addedUrls;

    private static final Pattern HTTPS_PATTERN = Pattern.compile("https?://[^/]+");
    private static final Pattern QUERY_ANCHOR_PATTERN = Pattern.compile("[?#].*$");

    public PagesUrlSummer(WorkerStarter workerStarter) {
        this.playwrightClient = workerStarter.getPlaywrightClient();
        this.parserType = workerStarter.getParserType();
        PagesUrlSummer.token = workerStarter.getToken();
        log.info("Token is fjp constructor: {}", token);

        this.parentSite = workerStarter.getParentSite();
        PageUrl initAddress = workerStarter.getAddress();

        Matcher matcher = HTTPS_PATTERN.matcher(initAddress.getAbsolutePath());
        this.headAddress = matcher.find() ? matcher.group() : "";

        if (initAddress.getPath() == null) {
            initAddress.setPath(normalizePath(initAddress.getAbsolutePath()));
        }

        // Регистрируем корневой URL, чтобы он не обрабатывался повторно
        addedUrls = ConcurrentHashMap.newKeySet();
        addedUrls.add(initAddress.getAbsolutePath());
        initAddress.setHeadUrl(headAddress);
        this.address = initAddress;
    }

    public PagesUrlSummer(PageUrl address,
                          PlaywrightClient playwrightClient,
                          String headAddress,
                          Site parentSite,
                          ParserType parserType) {
        this.headAddress = headAddress;
        this.parentSite = parentSite;
        this.playwrightClient = playwrightClient;
        address.setHeadUrl(headAddress);
        this.address = address;
        this.parserType = parserType;
    }

    @Override
    protected List<PageUrl> compute() {
        List<PageUrl> urls = new ArrayList<>();

        // Обрабатываем страницу, если удалось подключиться и получить контент
        if (!connection()) {
            return urls;
        }

        urls.add(address);

        List<PagesUrlSummer> taskList = new ArrayList<>();
        // Создаем задачи для всех дочерних URL, которые уже добавлены в children (это новые уникальные ссылки, добавленные в connection()).
        for (PageUrl child : address.getChildren()) {
            if (child != null) {
                PagesUrlSummer task = new PagesUrlSummer(child, playwrightClient, headAddress, parentSite, parserType);
                task.fork();
                taskList.add(task);
            }
        }

        for (PagesUrlSummer task : taskList) {
            try {
                List<PageUrl> result = task.join();
                if (result != null) {
                    urls.addAll(result);
                }
            } catch (Exception e) {
                log.error("Ошибка при выполнении задачи: {}", e.getMessage());
            }
        }

        return urls;
    }

    private boolean connection() {
        Document doc;
        if (parserType.equals(ParserType.JSOUP)) {
            try {
                Thread.sleep(150);
                doc = Jsoup.connect(address.getAbsolutePath())
                        .userAgent("HeliontSearchBot/1.0")
                        .referrer("http://www.google.com")
                        .timeout(10_000)
                        .get();
                log.info("Подключение jsoup: {}", address.getAbsolutePath());
            } catch (IOException | InterruptedException e) {
                log.error("Ошибка при подключении jsoup: {} address: {}", e.getMessage(), address.getAbsolutePath());
                return false;
            }
        } else if (parserType.equals(ParserType.PLAYWRIGHT)) {
            try {
                log.info("Подключение playwright: {}", address.getAbsolutePath());
                doc = Jsoup.parse(playwrightClient.getHtml(address.getAbsolutePath(), token));
            } catch (Exception e) {
                log.error("Ошибка при подключении playwright: {} address: {}", e.getMessage(), address.getAbsolutePath());
                return false;
            }
        } else {
            return false;
        }

        address.setContent(doc.toString());
        Elements elements = doc.select("a[href]");

        // Обрабатываем найденные ссылки на странице
        for (Element el : elements) {
            String href = el.attr("abs:href");
            if (href.isEmpty()) {
                href = el.attr("href");
            }
            href = normalizeUrl(href);

            // Если ссылка пустая, отфильтрована или уже добавлена – пропускаем её
            if (href.isEmpty() || urlFilter(href) || !addedUrls.add(href)) {
                continue;
            }

            if (href.startsWith(headAddress)) {
                addChildren(href, headAddress);
            }
        }
        return true;
    }

    private void addChildren(String url, String headAddress) {
        PageUrl newChild = new PageUrl();
        newChild.setChildren(new ArrayList<>());
        newChild.setAbsolutePath(url);
        newChild.setParent(this.address);
        newChild.setPath(normalizePath(url.replace(headAddress, "")));
        newChild.setHeadUrl(headAddress);
        newChild.setSite(parentSite);

        address.getChildren().add(newChild);
        log.info("Добавлен адрес: {}", newChild.getAbsolutePath());
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        // Удаляем якоря и параметры запроса
        url = QUERY_ANCHOR_PATTERN.matcher(url).replaceAll("");
        url = url.toLowerCase();
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        url = url.replaceAll("(?<!:)//+", "/");
        return url;
    }

    private String normalizePath(String path) {
        path = normalizeUrl(path);
        return path.startsWith("/") ? path.substring(1) : path;
    }

    private boolean urlFilter(String url) {
        return url.isEmpty() ||
                url.matches(".*\\.(jpg|png|pdf|svg|sql|jpeg|webp)$") ||
                url.contains("instagram") ||
                url.contains("tilda/click");
    }
}
