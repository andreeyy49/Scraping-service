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
public class Blog {

    private UUID id;

    private String blogText;

    private String path;

    private List<String> keyWords;

    private Date parseTime;

    private long siteId;

    private List<String> images;
}
