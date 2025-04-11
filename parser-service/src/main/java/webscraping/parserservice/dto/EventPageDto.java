package webscraping.parserservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import webscraping.parserservice.enums.SiteDataType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventPageDto {

    private String path;
    private SiteDataType siteDataType;

}
