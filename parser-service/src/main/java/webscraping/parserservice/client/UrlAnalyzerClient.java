package webscraping.parserservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import webscraping.parserservice.config.FeignConfig;
import webscraping.parserservice.enums.ParserType;

@FeignClient(name = "url-analyzer-service", url = "${external-api.urlAnalyzerService}", configuration = FeignConfig.class)
public interface UrlAnalyzerClient {

    @PostMapping("/parser-type")
    ParserType getParserType(@RequestBody String url, @RequestHeader("Authorization") String token);
}
