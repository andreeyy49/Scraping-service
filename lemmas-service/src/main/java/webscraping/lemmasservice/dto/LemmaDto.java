package webscraping.lemmasservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LemmaDto implements Serializable {

    private Long id;

    private Long siteId;

    private String lemma;

    private Integer frequency;

    private List<IndexDto> indexes = new ArrayList<>();
}
