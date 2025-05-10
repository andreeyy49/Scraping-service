package webscraping.entityvaultservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogTextChangeDto implements Serializable {

    private List<BlogDto> blogs;
}
