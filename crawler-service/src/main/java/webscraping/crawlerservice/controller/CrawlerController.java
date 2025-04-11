package webscraping.crawlerservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import webscraping.crawlerservice.services.IndexingService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/crawler")
public class CrawlerController {
    private final IndexingService indexingService;

    @GetMapping
    public void crawl(@RequestBody String url) {
        indexingService.startIndexing(url);
    }
}
