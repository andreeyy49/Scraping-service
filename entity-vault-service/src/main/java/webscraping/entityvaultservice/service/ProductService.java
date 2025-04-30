package webscraping.entityvaultservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import webscraping.entityvaultservice.dto.ProductDto;
import webscraping.entityvaultservice.model.Product;
import webscraping.entityvaultservice.repository.ProductRepository;
import webscraping.entityvaultservice.util.JoinEnum;

import java.util.*;
import java.util.function.Function;
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

    public Page<ProductDto> findAllProductsByTitleInPage(PageRequest request, String title) {
        List<ProductDto> filteredProductsDto = findAllProductsByTitle(title);
        return getPage(request, filteredProductsDto);
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

    public Page<ProductDto> findAllProductsByTitleAndSiteIdInPage(PageRequest request, String title, String siteId) {
        List<ProductDto> productsDto = findAllProductsByTitleAndSiteId(title, siteId);
        return getPage(request, productsDto);
    }

    public List<String> findCostProgress(Long siteId, String title) {
        List<ProductDto> products = findAllProductsByTitleAndSiteId(title, siteId.toString());
        return products.stream().map(ProductDto::getPrice).toList();
    }

    public Page<ProductDto> leftJoinProductsBySiteId(PageRequest request, Long siteId) {
        return getPage(request, joinProductsBySiteId(siteId, JoinEnum.LEFT));
    }

    public Page<ProductDto> rightJoinProductsBySiteId(PageRequest request, Long siteId) {
        return getPage(request, joinProductsBySiteId(siteId, JoinEnum.RIGHT));
    }

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

    private Page<ProductDto> getPage(PageRequest request, List<ProductDto> products) {
        int totalProducts = products.size();
        int start = (int) request.getOffset();
        int end = Math.min(start + request.getPageSize(), totalProducts);

        return new PageImpl<>(
                products.subList(start, end),
                request,
                totalProducts
        );
    }
}
