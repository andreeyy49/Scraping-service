package webscraping.entityvaultservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import webscraping.entityvaultservice.dto.ProductDto;
import webscraping.entityvaultservice.model.Blog;
import webscraping.entityvaultservice.model.Product;
import webscraping.entityvaultservice.repository.ProductRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public List<String> findLatestImageUrlsBySiteId(Long siteId) {
        List<ProductDto> products = findLatestBySiteId(siteId);

        return products.stream().map(ProductDto::getImageUrl).toList();
    }

    public List<ProductDto> findLatestBySiteId(Long siteId) {
        List<Product> products = productRepository.findAllBySiteId(siteId);

        Date lastDate = findLastDate(products);

        if (lastDate == null) {
            return new ArrayList<>();
        }

        return products.stream()
                .filter(product -> product.getParseTime().equals(lastDate))
                .map(this::productToDto)
                .toList();
    }

    public List<ProductDto> findAllProductsByTitle(String title) {
        List<Product> products = productRepository.findAllProductsByTitle(title);

        Map<Long, List<Product>> groupBySiteId = products.stream().collect(Collectors.groupingBy(Product::getSiteId));

        Map<Long, Date> lastDates = findLastDates(groupBySiteId);

        return groupBySiteId.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .filter(product -> {
                            Date lastDate = lastDates.get(entry.getKey());
                            return lastDate != null && product.getParseTime().compareTo(lastDate) == 0;
                        }))
                .map(this::productToDto)
                .toList();
    }


    public List<ProductDto> findAllProductsByTitleAndSiteId(String title, String siteId) {
        List<ProductDto> byTitle = findAllProductsByTitle(title);

        List<ProductDto> bySiteId = findLatestBySiteId(Long.parseLong(siteId));

        return byTitle.stream().filter(bySiteId::contains)
                .toList();
    }

    public List<String> findCostProgress(Long siteId, String title) {
        List<ProductDto> products = findAllProductsByTitleAndSiteId(title, siteId.toString());
        return products.stream().map(ProductDto::getPrice).toList();
    }


    private Date findLastDate(List<Product> products) {
        return products.stream()
                .map(Product::getParseTime)
                .max(Date::compareTo)
                .orElse(null);
    }

    private Map<Long, Date> findLastDates(Map<Long, List<Product>> products) {
        Map<Long, Date> lastDates = new HashMap<>();

        for (Map.Entry<Long, List<Product>> entry : products.entrySet()) {
            lastDates.put(entry.getKey(), entry.getValue().stream()
                    .map(Product::getParseTime)
                    .max(Date::compareTo)
                    .orElse(null));
        }

        return lastDates;
    }

    private ProductDto productToDto(Product product) {
        ProductDto productDto = new ProductDto();
        productDto.setId(String.valueOf(product.getId()));
        productDto.setName(product.getTitle());
        productDto.setPrice(product.getCost());
        productDto.setImageUrl(product.getImages().get(0));
        productDto.setProductUrl(product.getPath());
        return productDto;
    }
}
