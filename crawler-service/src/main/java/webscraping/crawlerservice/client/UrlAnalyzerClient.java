package webscraping.crawlerservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import webscraping.crawlerservice.config.FeignConfig;
import webscraping.crawlerservice.enums.ParserType;
import webscraping.crawlerservice.enums.SiteDataType;

@FeignClient(name = "url-analyzer-service", url = "${external-api.urlAnalyzerService}", configuration = FeignConfig.class)
public interface UrlAnalyzerClient {

    @PostMapping("/parser-type")
    ParserType getParserType(@RequestBody String url, @RequestHeader("Authorization") String token);

    @PostMapping("/site-type")
    SiteDataType getSiteType(@RequestBody String url, @RequestHeader("Authorization") String token);
}
