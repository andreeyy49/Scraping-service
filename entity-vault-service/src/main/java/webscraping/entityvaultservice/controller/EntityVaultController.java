package webscraping.entityvaultservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import webscraping.entityvaultservice.dto.ProductCostProgressDto;
import webscraping.entityvaultservice.dto.ProductDto;
import webscraping.entityvaultservice.model.Blog;
import webscraping.entityvaultservice.service.BlogService;
import webscraping.entityvaultservice.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/entity-vault")
@RequiredArgsConstructor
public class EntityVaultController {

    private final BlogService blogService;
    private final ProductService productService;

    @GetMapping("/blog/images/{site_id}")
    public List<String> findLatestImageUrlsBySiteIdInBlog(@PathVariable("site_id") Long siteId) {
        return blogService.findLatestImageUrlsBySiteId(siteId);
    }

    @PostMapping("/blog/findByKeyWords")
    public List<Blog> findByKeyWords(@RequestBody List<String> keywords) {
        return blogService.findLatestBlogsByKeywords(keywords);
    }

    @PostMapping("/blog/findByKeyWordsAndSiteId/{siteId}")
    public List<Blog> findByKeyWordsAndSiteId(@RequestBody List<String> keywords,
                                              @PathVariable("siteId") String siteId) {
        return blogService.findLatestBlogsByKeywordsAndSiteId(keywords, siteId);
    }

    @GetMapping("/product/images/{site_id}")
    public List<String> findLatestImageUrlsBySiteIdInProduct(@PathVariable("site_id") Long siteId) {
        return productService.findLatestImageUrlsBySiteId(siteId);
    }

    @GetMapping("/product/findAllByTitle/{title}")
    public Page<ProductDto> findAllProductsByTitle(@PathVariable("title") String title,
                                                   @RequestParam(name = "page", defaultValue = "0") int page,
                                                   @RequestParam(name = "size", defaultValue = "10") int size) {
        return productService.findAllProductsByTitleInPage(PageRequest.of(page, size), title);
    }

    @GetMapping("/product/findAllByTitleAndSiteId/{title}/{siteId}")
    public Page<ProductDto> findAllProductsByTitleAndSiteId(@PathVariable("title") String title,
                                                            @PathVariable("siteId") String siteId,
                                                            @RequestParam(name = "page", defaultValue = "0") int page,
                                                            @RequestParam(name = "size", defaultValue = "10") int size) {
        return productService.findAllProductsByTitleAndSiteIdInPage(PageRequest.of(page, size), title, siteId);
    }

    @PostMapping("/product/findCostProgress")
    public List<String> findCostProgress(@RequestBody ProductCostProgressDto productCostProgressDto) {
        return productService.findCostProgress(productCostProgressDto.getSiteId(),
                productCostProgressDto.getTitle());
    }

    @GetMapping("/product/findNew/{siteId}")
    public Page<ProductDto> findNewProducts(@PathVariable("siteId") Long siteId,
                                            @RequestParam(name = "page", defaultValue = "0") int page,
                                            @RequestParam(name = "size", defaultValue = "10") int size) {
        return productService.rightJoinProductsBySiteId(PageRequest.of(page, size), siteId);
    }

    @GetMapping("/product/findOld/{siteId}")
    public Page<ProductDto> findOldProducts(@PathVariable("siteId") Long siteId,
                                            @RequestParam(name = "page", defaultValue = "0") int page,
                                            @RequestParam(name = "size", defaultValue = "10") int size) {
        return productService.leftJoinProductsBySiteId(PageRequest.of(page, size), siteId);
    }
}
