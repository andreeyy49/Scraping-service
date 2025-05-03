package webscraping.entityvaultservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import webscraping.entityvaultservice.dto.ProductCostChangeDto;
import webscraping.entityvaultservice.dto.ProductDto;
import webscraping.entityvaultservice.model.Product;
import webscraping.entityvaultservice.repository.ProductRepository;
import webscraping.entityvaultservice.util.DateUtil;
import webscraping.entityvaultservice.util.JoinEnum;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceCache {

    private final ProductRepository productRepository;

    @Cacheable(value = "findLatestBySiteId", key = "#siteId")
    public List<ProductDto> findLatestBySiteId(Long siteId) {
        List<Product> products = productRepository.findAllBySiteId(siteId);

        Date lastDate = findLastDate(products);

        if (lastDate == null) {
            return new ArrayList<>();
        }

        return products.stream()
                .filter(product -> product.getParseTime().compareTo(lastDate) == 0)
                .map(this::productToDto)
                .toList();
    }

    @Cacheable(value = "findAllProductsByTitle", key = "'title' + #title")
    public List<ProductDto> findAllProductsByTitle(String title) {
        List<Product> products = productRepository.findAllProductsByTitle(title);

        Map<Long, List<Product>> groupBySiteId = products.stream().collect(Collectors.groupingBy(Product::getSiteId));

        Map<Long, Date> lastDates = findLastDates(groupBySiteId);

        if (lastDates.size() == 1) {
            return groupBySiteId.entrySet().stream()
                    .flatMap(entry -> entry.getValue().stream())
                    .map(this::productToDto)
                    .toList();
        }
        if (lastDates.isEmpty()) {
            return new ArrayList<>();
        }

        return groupBySiteId.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .filter(product -> {
                            Date lastDate = lastDates.get(entry.getKey());
                            return lastDate != null && product.getParseTime().compareTo(lastDate) == 0;
                        }))
                .map(this::productToDto)
                .toList();
    }


    @Cacheable(value = "joinProductsBySiteId", key = "#siteId + '-' + #joinEnum")
    public List<ProductDto> joinProductsBySiteId(Long siteId, JoinEnum joinEnum) {
        List<Date> dates = productRepository.findLastTwoUniqueDatesBySiteId(siteId);

        if (dates.isEmpty() || dates.size() < 2) {
            return new ArrayList<>();
        }

        List<Product> lastProducts = productRepository.findAllByParseTime(dates.get(0));
        List<Product> preLastProducts = productRepository.findAllByParseTime(dates.get(1));

        log.info("First date {}, second {}", dates.get(0), dates.get(1));
        log.info("New product size {}, old product size {}", lastProducts.size(), preLastProducts.size());

        Function<Product, String> key = k ->
                k.getTitle().toLowerCase().trim() + "|" + k.getSiteId();

        if (joinEnum == JoinEnum.LEFT) {
            Set<String> newProducts = lastProducts.stream()
                    .map(key)
                    .collect(Collectors.toSet());

            return preLastProducts.stream()
                    .filter(oldProduct -> !newProducts.contains(key.apply(oldProduct)))
                    .map(this::productToDto)
                    .collect(Collectors.toList());
        } else {
            Set<String> oldProducts = preLastProducts.stream()
                    .map(key)
                    .collect(Collectors.toSet());

            return lastProducts.stream()
                    .filter(newProduct -> !oldProducts.contains(key.apply(newProduct)))
                    .map(this::productToDto)
                    .collect(Collectors.toList());
        }
    }

    @Cacheable(value = "getPriceHistoryBySiteId", key = "'price' + #siteId")
    public List<ProductCostChangeDto> getPriceHistoryBySiteId(Long siteId) {
        List<Product> allProducts = productRepository.findAllBySiteId(siteId);

        Map<String, ProductCostChangeDto> result = new HashMap<>();

        for (Product product : allProducts) {
            String key = product.getTitle().toLowerCase().trim() + "|" + product.getSiteId();

            result.computeIfAbsent(key, k -> {
                ProductCostChangeDto dto = new ProductCostChangeDto();
                dto.setProduct(productToDto(product));
                dto.setCostChange(new TreeMap<>());
                return dto;
            });

            ProductCostChangeDto dto = result.get(key);

            if (!dto.getCostChange().containsValue(product.getCost())) {
                dto.getCostChange().put(DateUtil.formatToLocalDate(product.getParseTime()), product.getCost());
            }
        }

        return result.values().stream()
                .filter(dto -> dto.getCostChange().size() > 1)
                .toList();
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
