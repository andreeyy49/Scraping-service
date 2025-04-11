package webscraping.crawlerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import webscraping.crawlerservice.enums.SiteDataType;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class S3PagesDto {

    //Path + content
    private Map<String, String> pages;

    private SiteDataType siteDataType;
}
