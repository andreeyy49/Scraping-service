package webscraping.entityvaultservice.controller;

import lombok.RequiredArgsConstructor;
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
    public List<ProductDto> findAllProductsByTitle(@PathVariable("title") String title) {
        return productService.findAllProductsByTitle(title);
    }

    @GetMapping("/product/findAllByTitleAndSiteId/{title}/{siteId}")
    public List<ProductDto> findAllProductsByTitleAndSiteId(@PathVariable("title") String title,
                                                            @PathVariable("siteId") String siteId) {
        return productService.findAllProductsByTitleAndSiteId(title, siteId);
    }

    @PostMapping("/product/findCostProgress")
    public List<String> findCostProgress(@RequestBody ProductCostProgressDto productCostProgressDto) {
        return productService.findCostProgress(productCostProgressDto.getSiteId(),
                productCostProgressDto.getTitle());
    }
}
