package webscraping.crawlerservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import webscraping.crawlerservice.config.FeignConfig;

@FeignClient(name = "playwright-service", url = "${external-api.playwrightServiceUrl}", configuration = FeignConfig.class)
public interface PlaywrightClient {

    @GetMapping("/html")
    String getHtml(@RequestParam("url") String url, @RequestHeader("Authorization") String token);

    @GetMapping("/text")
    String getText(@RequestParam("url") String url, @RequestHeader("Authorization") String token);

    @GetMapping("/title")
    String getTitle(@RequestParam("url") String url, @RequestHeader("Authorization") String token);
}
