package webscraping.crawlerservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import webscraping.crawlerservice.dto.SiteDto;
import webscraping.crawlerservice.services.IndexingService;
import webscraping.crawlerservice.services.SiteService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/crawler")
public class CrawlerController {
    private final IndexingService indexingService;
    private final SiteService siteService;

    @GetMapping
    public void crawl(@RequestParam String url) {
        indexingService.startIndexing(url);
    }

    @GetMapping("/getAllSitesStats")
    public List<SiteDto> getAllSitesStats() {
        return siteService.getAllSitesStats();
    }

    @GetMapping("/e-comers")
    public List<SiteDto> getEComers() {
        return siteService.getEComers();
    }

    @GetMapping("/blogs")
    public List<SiteDto> getBlogs() {
        return siteService.getBlogs();
    }
}
