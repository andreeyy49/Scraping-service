package webscraping.entityvaultservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogDto implements Serializable {

    private String blogText;

    private String path;

    private String parseTime;

    private long siteId;

    private List<String> keyWords;

    private List<String> images;
}
