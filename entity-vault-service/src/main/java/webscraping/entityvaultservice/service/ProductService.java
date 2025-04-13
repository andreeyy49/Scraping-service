package webscraping.entityvaultservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import webscraping.entityvaultservice.model.Product;
import webscraping.entityvaultservice.repository.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public List<String> findLatestImageUrlsBySiteId(Long siteId) {
        return productRepository.findLatestImageUrlsBySiteId(siteId);
    }

    public List<Product> findAllProductsByTitle(String title) {
        return productRepository.findAllProductsByTitle(title);
    }

    public List<String> findCostProgress(Long siteId, String title) {
        List<Product> products = productRepository.findAllProductsBySiteIdAndTitle(siteId, title);
        return products.stream().map(Product::getCost).toList();
    }
}
