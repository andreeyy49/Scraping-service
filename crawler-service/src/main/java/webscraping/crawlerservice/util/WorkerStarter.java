package webscraping.crawlerservice.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import webscraping.crawlerservice.client.PlaywrightClient;
import webscraping.crawlerservice.enums.ParserType;
import webscraping.crawlerservice.model.PageUrl;
import webscraping.crawlerservice.model.Site;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WorkerStarter {
    private PageUrl address;
    private PlaywrightClient playwrightClient;
    private Site parentSite;
    private String token;
    private String headAddress;
    private ParserType parserType;
}
