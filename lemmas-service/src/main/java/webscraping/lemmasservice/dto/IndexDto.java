package webscraping.lemmasservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexDto implements Serializable {

    private Long id;

    private UUID pageId;

    private Integer rank;
}
