package webscraping.crawlerservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import webscraping.crawlerservice.client.PlaywrightClient;
import webscraping.crawlerservice.client.UrlAnalyzerClient;
import webscraping.crawlerservice.enums.SiteDataType;
import webscraping.crawlerservice.model.Page;
import webscraping.crawlerservice.model.PageUrl;
import webscraping.crawlerservice.model.Site;
import webscraping.crawlerservice.model.Status;
import webscraping.crawlerservice.util.PageMapper;
import webscraping.crawlerservice.util.UserContext;
import webscraping.crawlerservice.util.WorkerStarter;
import webscraping.crawlerservice.worker.PagesUrlSummer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingService {

    private final SiteService siteService;

    private final PageService pageService;

    private final PlaywrightClient playwrightClient;

    private final UrlAnalyzerClient analyzerClient;

    private final ForkJoinPool forkJoinPool;

    private static boolean isStartIndexing;

    private String token;

//    private final EventService eventService;
    private final S3Service s3Service;

//    private static final Pattern HTTPS_PATTERN = Pattern.compile("https://[^/]+");

    private final int processorSize = Runtime.getRuntime().availableProcessors();

    public void startIndexing(String url) {

        pageService.deleteBySiteIsNull();

        if (isStartIndexing) {
            throw new IllegalStateException("Индексция уже запущена");
        }

        token = UserContext.getToken();

        Thread thread = new Thread(() -> {

            long start = System.currentTimeMillis();

            isStartIndexing = true;

            String normalUrl = normalizeUrl(url);
            String[] splitUrl = normalUrl.split("/");

            Site site = siteService.findByUrl(normalUrl);
            if(site != null) {
                site.setStatus(Status.INDEXING);
                site.setStatusTime(Instant.now());
                siteService.update(site);
            } else {
                site = new Site();
                site.setUrl(normalUrl);
                site.setStatus(Status.INDEXING);
                site.setName(splitUrl[2]);
                site.setStatusTime(Instant.now());
                siteService.save(site);
            }

            PageUrl pageUrl = new PageUrl();
            pageUrl.setSite(site);
            pageUrl.setAbsolutePath(site.getUrl());

            List<Future<List<PageUrl>>> pagesUrlSummerFuture = new ArrayList<>();

            WorkerStarter workerStarter = new WorkerStarter();
            workerStarter.setPlaywrightClient(playwrightClient);
            workerStarter.setToken(token);

            SiteDataType siteDataType = analyzerClient.getSiteType(site.getUrl(), token);

            workerStarter.setAddress(pageUrl);
            workerStarter.setParentSite(pageUrl.getSite());
            workerStarter.setParserType(analyzerClient.getParserType(pageUrl.getAbsolutePath(), token));
            pagesUrlSummerFuture.add(forkJoinPool.submit(new PagesUrlSummer(workerStarter)));

//
//            for (Future<List<PageUrl>> pagesUrlSummer : pagesUrlSummerFuture) {
//                while (!pagesUrlSummer.isDone()) {
//                    stopWorkers(sitesToDb, forkJoinPool);
//                }
//            }

            List<PageUrl> allPageUrls = new ArrayList<>();
            for (Future<List<PageUrl>> pagesUrlSummer : pagesUrlSummerFuture) {
                try {
                    List<PageUrl> result = pagesUrlSummer.get(); // блокирующий вызов, безопасный после submit
                    if (result != null) {
                        allPageUrls.addAll(result);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Ошибка при ожидании результата: {}", e.getMessage());
                }
            }



            site.setStatus(Status.INDEXED);
            site.setStatusTime(Instant.now());
            Site updatedSite = siteService.update(site);

            List<Page> pageListToDb = allPageUrls.stream().map(el -> PageMapper.pageUrlToPage(el, updatedSite)).toList();

            pageService.saveAll(pageListToDb);

            isStartIndexing = false;

            log.info("Indexing finished {}ms", System.currentTimeMillis() - start);

            if (!pageListToDb.isEmpty()) {
                s3Service.uploadPagesJson(pageListToDb, siteDataType);
//                eventService.sendPagesToTopic(pageListToDb, siteDataType);
            }
        });

        thread.start();
    }

    private String normalizeUrl(String url) {
        if(!url.endsWith("/")) {
            return url + "/";
        }
        return url;
    }

    public void stopIndexing() {
        if (isStartIndexing) {
            isStartIndexing = false;
        } else {
            throw new IllegalStateException("Индексация не запущена");
        }
    }

//    @SneakyThrows
//    public void indexPage(String url) {
//        long startTime = System.currentTimeMillis();
//
//        String pageUrl;
//
//        String siteUrl = "";
//
//        Matcher matcher = HTTPS_PATTERN.matcher(url);
//
//        while (matcher.find()) {
//            int start = matcher.start();
//            int end = matcher.end();
//            siteUrl = url.substring(start, end);
//        }
//
//        pageUrl = url.replace(siteUrl, "");
//
//        Page page = pageService.findByPath(pageUrl);
//
//        if (page != null) {
//            pageService.delete(page.getId());
//        } else {
//            page = pageService.initPage(url, pageUrl);
//            pageService.save(page);
//        }
//
//        String content = page.getContent();
//
//        HashMap<String, Integer> lemmas = LemmasFinder.getLemmasHashMap(content);
//
//        log.info("Starting get LemmasHashMap");
//
//        List<String> lemmasInDb = lemmaService.findAllLemmaValue();
//        if (lemmasInDb == null) {
//            lemmasInDb = new ArrayList<>();
//        }
//
//        List<Lemma> lemmasToSave = new ArrayList<>();
//        List<Index> indexesToSave = new ArrayList<>();
//
//        for (HashMap.Entry<String, Integer> entry : lemmas.entrySet()) {
//            Index index = new Index();
//            index.setPage(page);
//            index.setRank(entry.getValue());
//            Lemma lemma = new Lemma();
//            lemma.setLemma(entry.getKey());
//            lemma.setFrequency(1);
//            lemma.setSite(page.getSite());
//            List<Index> indexes = new ArrayList<>();
//            indexes.add(index);
//            lemma.setIndexes(indexes);
//            index.setLemma(lemma);
//            lemmasToSave.add(lemma);
//            indexesToSave.add(index);
//
//            lemmasInDb.add(entry.getKey());
//        }
//
//        lemmaService.saveAll(lemmasToSave);
//        indexService.saveAll(indexesToSave);
//
//        log.info("indexing finished {}ms", System.currentTimeMillis() - startTime);
//    }
//
//    @SneakyThrows
//    public List<DataResponse> search(String query, String site, Integer offset, Integer limit) {
//        if (offset == null) {
//            offset = 0;
//        }
//        if (limit == null) {
//            limit = 20;
//        }
//
//        if (query.isEmpty()) {
//            throw new IllegalStateException("Задан пустой поисковый запрос!");
//        }
//
//        HashMap<String, Integer> queryLemmas = LemmasFinder.getLemmasHashMap(query);
//        List<Lemma> lemmasInDb = new ArrayList<>();
//        List<Page> allPages = pageService.findAll();
//        int totalPagesSize = allPages.size();
//        List<String> sitesUrl = new ArrayList<>();
//
//        if (site == null) {
//            sitesUrl.addAll(siteService.findAll().stream().map(siteElement -> {
//                if (siteElement.getStatus().equals(Status.INDEXED)) {
//                    return siteElement.getUrl();
//                }
//                return null;
//            }).filter(Objects::nonNull).toList());
//        } else {
//            sitesUrl.add(site);
//        }
//
//        if (sitesUrl.isEmpty()) {
//            throw new IllegalStateException("Выбранные сайты не проиндексированны!");
//        }
//
//        if (sitesUrl.size() == 1) {
//            Site siteItem = siteService.findByUrl(sitesUrl.get(0));
//            if(!siteItem.getStatus().equals(Status.INDEXED)) {
//                throw new IllegalStateException("Выбранный сайт не проиндексирован!");
//            }
//        }
//
//        for (String key : queryLemmas.keySet()) {
//            for (String siteUrl : sitesUrl) {
//                Lemma lemma = lemmaService.findByLemma(key, siteUrl);
//                if (lemma != null && (checkPercent(80, totalPagesSize, lemma.getFrequency()) || queryLemmas.size() == 1)) {
//                    lemmasInDb.add(lemma);
//                }
//            }
//        }
//
//        if (lemmasInDb.isEmpty()) {
//            return new ArrayList<>();
//        }
//
//        lemmasInDb.sort(Comparator.comparing(Lemma::getFrequency));
//
//        List<Long> firstLemmaIndexIds = new ArrayList<>();
//        lemmasInDb.get(0).getIndexes().forEach(index -> firstLemmaIndexIds.add(index.getId()));
//        List<Page> pagesResult = new ArrayList<>(pageService.findAllByIndexes(firstLemmaIndexIds));
//
//        for (Lemma lemma : lemmasInDb) {
//            List<Page> tempPages = new ArrayList<>();
//            List<Long> indexIds = new ArrayList<>();
//            if (lemma != lemmasInDb.get(0)) {
//                lemma.getIndexes().forEach(index -> indexIds.add(index.getId()));
//                List<Page> lemmasPages = new ArrayList<>(pageService.findAllByIndexes(indexIds));
//                for (Page page : lemmasPages) {
//                    if (pagesResult.contains(page)) {
//                        tempPages.add(page);
//                    }
//                }
//                pagesResult = new ArrayList<>(tempPages);
//            }
//        }
//
//        List<LemmaPageRank> lemmasPageRanks = new ArrayList<>();
//
//        for (Lemma lemma : lemmasInDb) {
//            for (Index index : lemma.getIndexes()) {
//                if (pagesResult.contains(index.getPage())) {
//                    Page page = index.getPage();
//                    boolean pageExists = false;
//
//                    for (LemmaPageRank lemmaPageRank : lemmasPageRanks) {
//                        if (lemmaPageRank.getPage().equals(page)) {
//                            // Если сущность с этой страницей уже есть, добавляем новую лему в Map
//                            lemmaPageRank.getLemmaRank().put(lemma, index.getRank());
//                            pageExists = true;
//                            break; // Останавливаем поиск, т.к. сущность найдена
//                        }
//                    }
//
//                    // Если сущность с этой страницей не найдена, создаем новую
//                    if (!pageExists) {
//                        HashMap<Lemma, Integer> lemmaRanks = new HashMap<>();
//                        lemmaRanks.put(lemma, index.getRank());
//                        lemmasPageRanks.add(new LemmaPageRank(lemmaRanks, page));
//                    }
//                }
//            }
//        }
//
//        lemmasPageRanks.forEach(LemmaPageRank::calculateRelevance);
//        Float max = 0F;
//        for (LemmaPageRank lemmaPageRank : lemmasPageRanks) {
//            if (lemmaPageRank.getThisRelevance() > max) {
//                max = lemmaPageRank.getThisRelevance();
//            }
//        }
//
//        for (LemmaPageRank lemmaPageRank : lemmasPageRanks) {
//            lemmaPageRank.setTotalRelevance(max);
//            lemmaPageRank.calculateTotalRelevance();
//        }
//
//        lemmasPageRanks.sort(Comparator.comparing(LemmaPageRank::getTotalRelevance).reversed());
//        List<DataResponse> dataResponses = new ArrayList<>();
//
//        for (int i = offset; i < lemmasPageRanks.size(); i++) {
//            if (i >= limit) {
//                break;
//            }
//            DataResponse dataResponse = new DataResponse();
//
//            dataResponse.setUri(lemmasPageRanks.get(i).getPage().getPath());
//            dataResponse.setRelevance(lemmasPageRanks.get(i).getTotalRelevance());
//            Site siteOnPage = lemmasPageRanks.get(i).getPage().getSite();
//            dataResponse.setSite(siteOnPage.getUrl());
//            dataResponse.setSiteName(siteOnPage.getName());
//            String pageContent = lemmasPageRanks.get(i).getPage().getContent();
//            Document document = Jsoup.parse(pageContent);
//            dataResponse.setTitle(document.title());
//            List<String> lemmaList = new ArrayList<>(lemmasPageRanks.get(i).getLemmaRank().keySet().stream().map(Lemma::getLemma).toList());
//            pageContent = LemmasFinder.extractFragmentsWithHighlight(pageContent, lemmaList);
//
//            if (pageContent.length() > 240) {
//                pageContent = pageContent.substring(0, 270);
//                while (!pageContent.endsWith(" ")) {
//                    pageContent = pageContent.substring(0, pageContent.length() - 1);
//                }
//                if (pageContent.endsWith(",")) {
//                    pageContent = pageContent.substring(0, pageContent.length() - 1);
//                }
//            }
//            dataResponse.setSnippet(pageContent);
//
//            dataResponses.add(dataResponse);
//        }
//
//        return dataResponses;
//
//    }

    private boolean checkPercent(int percent, int sizePages, int frequency) {
        int result = frequency * 100 / sizePages;
        return percent >= result;
    }

    private void stopWorkers(List<Site> sitesToDb, ForkJoinPool forkJoinPool) {
        if (!isStartIndexing) {
            for (Site site : sitesToDb) {
                site.setLastError("Индексация остановлена пользователем");
                site.setStatus(Status.FAILED);
                site.setStatusTime(Instant.now());
                siteService.update(site);
            }
            forkJoinPool.shutdownNow();
            log.info("Индексация остановлена пользователем");
        }
    }
}