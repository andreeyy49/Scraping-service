package webscraping.crawlerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import webscraping.crawlerservice.enums.SiteDataType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventPageDto {

    private String path;
    private SiteDataType siteDataType;

}
