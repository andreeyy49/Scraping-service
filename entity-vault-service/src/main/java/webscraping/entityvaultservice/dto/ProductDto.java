package webscraping.entityvaultservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto implements Serializable {
    private String id;
    private String name;
    private String price;
    private String imageUrl;
    private String productUrl;
}
