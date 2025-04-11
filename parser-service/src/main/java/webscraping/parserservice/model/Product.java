package webscraping.parserservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    private UUID id;

    private String title;

    private String cost;

    private String path;

    private Date parseTime;

    private long siteId;

    private List<String> images;
}
