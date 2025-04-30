package webscraping.entityvaultservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductCostChangeDto {

    private ProductDto product;
    private Map<Date, String> costChange;
}
