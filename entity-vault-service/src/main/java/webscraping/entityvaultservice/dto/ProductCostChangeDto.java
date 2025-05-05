package webscraping.entityvaultservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductCostChangeDto implements Serializable {

    private ProductDto product;
    private Map<String, String> costChange;
}
