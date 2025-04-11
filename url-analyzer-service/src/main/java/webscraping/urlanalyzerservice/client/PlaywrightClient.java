package webscraping.urlanalyzerservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import webscraping.urlanalyzerservice.config.PlaywrightFeignConfig;

@FeignClient(name = "playwright-service", url = "${external-api.playwrightServiceUrl}", configuration = PlaywrightFeignConfig.class)
public interface PlaywrightClient {

    @GetMapping("/html")
    String getHtml(@RequestParam("url") String url);

    @GetMapping("/text")
    String getText(@RequestParam("url") String url);

    @GetMapping("/title")
    String getTitle(@RequestParam("url") String url);
}
