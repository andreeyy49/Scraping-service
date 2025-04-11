package webscraping.playwrightservice.service;

import com.microsoft.playwright.*;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import webscraping.playwrightservice.exception.BadRequestException;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class PlaywrightService {

    private final Playwright playwright;
    private final Browser browser;

    public PlaywrightService() {
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    public CompletableFuture<String> getPageHtml(String url) {
        return fetchPage(url, Page::content);
    }

    public CompletableFuture<String> getPageContent(String url) {
        return fetchPage(url, page -> {
            page.waitForSelector("body", new Page.WaitForSelectorOptions().setTimeout(5000));
            return page.innerText("body");
        });
    }

    public CompletableFuture<String> getPageTitle(String url) {
        return fetchPage(url, Page::title);
    }

    private CompletableFuture<String> fetchPage(String url, PageFunction action) {
        return CompletableFuture.supplyAsync(() -> {
            try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
                configurePage(page);
                page.navigate(url);
                return action.apply(page);
            } catch (PlaywrightException e) {
                throw new BadRequestException("Error while fetching page: " + e.getMessage());
            }
        });
    }

    private void configurePage(Page page) {
        page.addInitScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
        page.setExtraHTTPHeaders(Map.of(
                "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
                "Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3",
                "Referer", "https://www.google.com/"
        ));
    }

    @PreDestroy
    public void endSession() {
        browser.close();
        playwright.close();
    }

    @FunctionalInterface
    private interface PageFunction {
        String apply(Page page);
    }
}
