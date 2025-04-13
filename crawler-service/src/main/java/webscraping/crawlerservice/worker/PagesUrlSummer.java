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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
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
    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static final Map<String, Semaphore> hostSemaphores = new ConcurrentHashMap<>();
    private static final Semaphore globalSemaphore = new Semaphore(30); // Увеличено до 30
    private static final int MAX_HOST_CONCURRENT_REQUESTS = 15; // Лимит на хост

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
        Document doc = null;
        if (parserType.equals(ParserType.JSOUP)) {
            int retries = 3;
            while (retries > 0) {
                try {
                    log.info("Подключение HttpClient: {}", address.getAbsolutePath());
                    doc = fetchWithSemaphore(address.getAbsolutePath()).join();
                    break; // Успех - выходим из цикла
                } catch (Exception e) {
                    retries--;
                    if (retries == 0) {
                        log.error("Ошибка при подключении HttpClient: {} address: {}",
                                e.getMessage(), address.getAbsolutePath());
                        return false;
                    }
                    log.warn("Повторная попытка ({}) для {}: {}",
                            retries, address.getAbsolutePath(), e.getMessage());
                    try {
                        Thread.sleep(1000 * (4 - retries)); // Экспоненциальная задержка
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
//        }
//        if (parserType.equals(ParserType.JSOUP)) {
//            try {
//                Thread.sleep(150); // Пауза между запросами
//                doc = Jsoup.connect(address.getAbsolutePath())
//                        .userAgent("HeliontSearchBot/1.0")
//                        .referrer("http://www.google.com")
//                        .timeout(10_000)
//                        .get();
//                log.info("Подключение jsoup: {}", address.getAbsolutePath());
//            } catch (IOException | InterruptedException e) {
//                log.error("Ошибка при подключении jsoup: {} address: {}", e.getMessage(), address.getAbsolutePath());
//                return false;
//            }
        } else if (parserType.equals(ParserType.PLAYWRIGHT)) {
            try {
                log.info("Подключение playwright: {}", address.getAbsolutePath());
                doc = Jsoup.parse(playwrightClient.getHtml(address.getAbsolutePath(), token));
            } catch (Exception e) {
                log.error("Ошибка при подключении playwright: {} address: {}", e.getMessage(), address.getAbsolutePath());
                return false;
            }
//        } else if (parserType.equals(ParserType.JSOUP)) {
//            try {
//                log.info("Подключение HttpClient: {}", address.getAbsolutePath());
//                doc = fetchContentWithHttpClient(address.getAbsolutePath());
//            } catch (Exception e) {
//                log.error("Ошибка при подключении HttpClient: {} address: {}", e.getMessage(), address.getAbsolutePath());
//                return false;
//            }
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

    private CompletableFuture<Document> fetchWithSemaphore(String url) {
        String host = URI.create(url).getHost();
        Semaphore hostSemaphore = hostSemaphores.computeIfAbsent(host,
                h -> new Semaphore(MAX_HOST_CONCURRENT_REQUESTS));
        try {

            // Пробуем получить разрешения с небольшим таймаутом
            if (!globalSemaphore.tryAcquire(1, 50, TimeUnit.MILLISECONDS)) {
                log.debug("Global semaphore timeout for {}", url);
                return CompletableFuture.failedFuture(new IOException("Global request limit exceeded"));
            }

            if (!hostSemaphore.tryAcquire(1, 50, TimeUnit.MILLISECONDS)) {
                globalSemaphore.release();
                log.debug("Host semaphore timeout for {}", host);
                return CompletableFuture.failedFuture(new IOException("Host request limit exceeded"));
            }

            // Динамическая задержка в зависимости от загрузки
            int available = globalSemaphore.availablePermits();
            if (available < 10) {
                Thread.sleep(30 + (10 - available) * 15);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(e);
        }

//        // Настраиваем HTTP клиент с retry политикой
//        HttpClient client = HttpClient.newBuilder()
//                .version(HttpClient.Version.HTTP_1_1)
//                .connectTimeout(Duration.ofSeconds(15))
//                .followRedirects(HttpClient.Redirect.NORMAL)
//                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (compatible; MyBot/1.0; +http://mysite.com/bot.html)")
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        CompletableFuture<HttpResponse<String>> responseFuture = client.sendAsync(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        return responseFuture
                .thenApplyAsync(response -> {
                    if (response.statusCode() >= 400) {
                        throw new RuntimeException("HTTP error: " + response.statusCode());
                    }
                    return Jsoup.parse(response.body(), url);
                })
                .whenComplete((res, ex) -> {
                    // Всегда освобождаем семафоры
                    hostSemaphore.release();
                    globalSemaphore.release();

                    if (ex != null) {
                        log.debug("Failed to fetch {}: {}", url, ex.getMessage());
                    }
                });
    }

}
