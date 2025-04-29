package webscraping.crawlerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import webscraping.crawlerservice.model.Status;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SiteDto {

    private String id;
    private String url;
    private String type;
    private String name;
    private Integer pageCount;
    private Status status;
    private String lastScraped;
}
