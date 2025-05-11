package webscraping.lemmasservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogDto {

    private UUID id;

    private String blogText;

    private String path;

    private Date parseTime;

    private long siteId;

    private String hash;

    private List<String> keyWords;

    private List<String> images;
}
