package webscraping.entityvaultservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import webscraping.entityvaultservice.dto.PageDto;
import webscraping.entityvaultservice.dto.ProductCostChangeDto;
import webscraping.entityvaultservice.dto.ProductDto;
import webscraping.entityvaultservice.model.Product;
import webscraping.entityvaultservice.repository.ProductRepository;
import webscraping.entityvaultservice.util.JoinEnum;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    private final ProductServiceCache productServiceCache;

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public List<String> findLatestImageUrlsBySiteId(Long siteId) {
        try {
            List<ProductDto> products = productServiceCache.findLatestBySiteId(siteId);
            return products.stream().map(ProductDto::getImageUrl).toList();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }


    @CacheEvict(value = {"findAllProductsByTitle"}, allEntries = true)
    public PageDto<ProductDto> findAllProductsByTitleInPage(PageRequest request, String title) {
        try {
            return getPage(request, productServiceCache.findAllProductsByTitle(title));
        } catch (
                Exception e) {
            log.error(e.getMessage());
        }

        return null;
    }

    public List<ProductDto> findAllProductsByTitleAndSiteId(String title, Long siteId) {
        try {
            List<ProductDto> byTitle = productServiceCache.findAllProductsByTitle(title);

            if (byTitle == null || byTitle.isEmpty()) {
                log.warn("by title is empty");
                return Collections.emptyList();
            } else {
                log.info("by title: {}", byTitle.size());
            }

            List<ProductDto> bySiteId = new ArrayList<>();

            try {
                bySiteId = productServiceCache.findLatestBySiteId(siteId);
            } catch (Exception e) {
                log.error("cache: {}", e.getMessage());
            }
            if (bySiteId == null || bySiteId.isEmpty()) {
                log.warn("by siteId is empty");
                return Collections.emptyList();
            } else {
                log.info("by siteId: {}", bySiteId.size());
            }
            return bySiteId.stream().filter(byTitle::contains)
                    .toList();
        } catch (
                Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public PageDto<ProductDto> findAllProductsByTitleAndSiteIdInPage(PageRequest request, String title, Long siteId) {
        return getPage(request, findAllProductsByTitleAndSiteId(title, siteId));
    }

    public PageDto<ProductDto> leftJoinProductsBySiteId(PageRequest request, Long siteId) {
        try {
            return getPage(request, productServiceCache.joinProductsBySiteId(siteId, JoinEnum.LEFT));
        } catch (
                Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public PageDto<ProductDto> rightJoinProductsBySiteId(PageRequest request, Long siteId) {
        try {
            return getPage(request, productServiceCache.joinProductsBySiteId(siteId, JoinEnum.RIGHT));
        } catch (
                Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public PageDto<ProductCostChangeDto> getPriceHistoryBySiteIdInPage(PageRequest request, Long siteId) {
        try {
            return getPage(request, productServiceCache.getPriceHistoryBySiteId(siteId));
        } catch (
                Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    private <T> PageDto<T> getPage(PageRequest request, List<T> products) {
        int totalProducts = products.size();
        int start = (int) request.getOffset();
        int end = Math.min(start + request.getPageSize(), totalProducts);
        int surplus = totalProducts % request.getPageSize();
        if (surplus > 0) {
            surplus = 1;
        }
        int totalPages = totalProducts / request.getPageSize() + surplus;

        List<T> content = products.subList(start, end);

        return new PageDto<>(content, request.getPageNumber(), request.getPageSize(), totalProducts, totalPages);
    }
}
