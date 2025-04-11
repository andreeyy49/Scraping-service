package webscraping.playwrightservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import webscraping.playwrightservice.exception.BadRequestException;
import webscraping.playwrightservice.service.PlaywrightService;

import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/playwright")
@Slf4j
public class PlaywrightController {

    private final PlaywrightService playwrightService;

    @GetMapping("/html")
    public String getHtml(@RequestParam String url) {
        try {
            log.info("Get HTML for url {}", url);
            return playwrightService.getPageHtml(url).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @GetMapping("/text")
    public String getText(@RequestParam String url) {
        try {
            log.info("Get TEXT for url {}", url);
            return playwrightService.getPageContent(url).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @GetMapping("/title")
    public String getTitle(@RequestParam String url) {
        try {
            log.info("Get TITLE for url {}", url);
            return playwrightService.getPageTitle(url).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

}
