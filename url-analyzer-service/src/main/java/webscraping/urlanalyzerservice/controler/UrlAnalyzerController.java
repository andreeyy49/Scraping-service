package webscraping.urlanalyzerservice.controler;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import webscraping.urlanalyzerservice.enums.ParserType;
import webscraping.urlanalyzerservice.service.UrlAnalyzeService;
import webscraping.urlanalyzerservice.enums.SiteDataType;

@RestController
@RequestMapping("/api/v1/url-analyzer")
@RequiredArgsConstructor
public class UrlAnalyzerController {
    private final UrlAnalyzeService urlAnalyzeService;

    @PostMapping("/site-type")
    public SiteDataType getSiteType(@RequestBody String url) {
        return urlAnalyzeService.analyze(url);
    }

    @PostMapping("/parser-type")
    public ParserType getParserType(@RequestBody String url) {
        return urlAnalyzeService.getParserType(url);
    }

}
