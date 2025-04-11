package webscraping.urlanalyzerservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import webscraping.urlanalyzerservice.enums.SiteDataType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SiteStructure {
    private String title;
    private String body;
    private SiteDataType siteDataType;
}