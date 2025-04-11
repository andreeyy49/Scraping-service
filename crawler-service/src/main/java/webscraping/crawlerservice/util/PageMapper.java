package webscraping.crawlerservice.util;

import webscraping.crawlerservice.model.Page;
import webscraping.crawlerservice.model.PageUrl;
import webscraping.crawlerservice.model.Site;

import java.util.UUID;

public class PageMapper {

    public static Page pageUrlToPage(PageUrl pageUrl, Site site) {
        Page page = new Page();
        page.setId(UUID.randomUUID());
        page.setCode(200);
        page.setPath(pageUrl.getAbsolutePath());
        page.setSite(site);
        page.setContent(pageUrl.getContent());

        return page;
    }
}
