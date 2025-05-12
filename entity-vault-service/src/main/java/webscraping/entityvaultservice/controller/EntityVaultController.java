package webscraping.entityvaultservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import webscraping.entityvaultservice.dto.*;
import webscraping.entityvaultservice.service.BlogService;
import webscraping.entityvaultservice.service.ProductService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/entity-vault")
@RequiredArgsConstructor
public class EntityVaultController {

    private final BlogService blogService;
    private final ProductService productService;

    @GetMapping("/blog/allCategory")
    public List<String> findAllCategory() {
        return blogService.findAllCategory();
    }

    @PostMapping("/blog/findByIds")
    public PageDto<BlogDto> findByIds(@RequestBody List<UUID> blogIds,
                                      @RequestParam(name = "page", defaultValue = "0") int page,
                                      @RequestParam(name = "size", defaultValue = "10") int size) {
        return blogService.findByIds(PageRequest.of(page, size), blogIds);
    }

    @GetMapping("/blog/images/{site_id}")
    public List<String> findLatestImageUrlsBySiteIdInBlog(@PathVariable("site_id") Long siteId) {
        return blogService.findLatestImageUrlsBySiteId(siteId);
    }

    @PostMapping("/blog/findByKeyWords")
    public PageDto<BlogDto> findByKeyWords(@RequestBody List<String> keywords,
                                           @RequestParam(name = "page", defaultValue = "0") int page,
                                           @RequestParam(name = "size", defaultValue = "10") int size) {
        PageDto<BlogDto> pageDto = blogService.findLatestBlogsByKeywordsInPage(PageRequest.of(page, size), keywords);
        log.info("Blogs count: {} \n page: {}, size: {}", pageDto.getContent().size(), page, size);
        return pageDto;
    }

    @PostMapping("/blog/findByKeyWordsAndSiteId/{siteId}")
    public PageDto<BlogDto> findByKeyWordsAndSiteId(@RequestBody List<String> keywords,
                                                    @PathVariable("siteId") Long siteId,
                                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                                    @RequestParam(name = "size", defaultValue = "10") int size) {
        return blogService.findLatestBlogsByKeywordsAndSiteIdInPage(PageRequest.of(page, size), keywords, siteId);
    }

    @GetMapping("/blog/findChangedBlogs/{siteId}")
    public PageDto<BlogTextChangeDto> findChangedBlogs(@PathVariable("siteId") Long siteId,
                                                       @RequestParam(name = "page", defaultValue = "0") int page,
                                                       @RequestParam(name = "size", defaultValue = "10") int size) {
        return blogService.findChangedBlogsInPage(PageRequest.of(page, size), siteId);
    }

    @GetMapping("/blog/findNew/{siteId}")
    public PageDto<BlogDto> findNewBlogs(@PathVariable("siteId") Long siteId,
                                               @RequestParam(name = "page", defaultValue = "0") int page,
                                               @RequestParam(name = "size", defaultValue = "10") int size) {
        return blogService.rightJoinBlogsBySiteId(PageRequest.of(page, size), siteId);
    }

    @GetMapping("/blog/findOld/{siteId}")
    public PageDto<BlogDto> findOldBlogs(@PathVariable("siteId") Long siteId,
                                               @RequestParam(name = "page", defaultValue = "0") int page,
                                               @RequestParam(name = "size", defaultValue = "10") int size) {
        return blogService.leftJoinBlogsBySiteId(PageRequest.of(page, size), siteId);
    }

    @GetMapping("/product/images/{site_id}")
    public List<String> findLatestImageUrlsBySiteIdInProduct(@PathVariable("site_id") Long siteId) {
        return productService.findLatestImageUrlsBySiteId(siteId);
    }

    @GetMapping("/product/findAllByTitle/{title}")
    public PageDto<ProductDto> findAllProductsByTitle(@PathVariable("title") String title,
                                                      @RequestParam(name = "page", defaultValue = "0") int page,
                                                      @RequestParam(name = "size", defaultValue = "10") int size) {
        return productService.findAllProductsByTitleInPage(PageRequest.of(page, size), title);
    }

    @GetMapping("/product/findAllByTitleAndSiteId/{title}/{siteId}")
    public PageDto<ProductDto> findAllProductsByTitleAndSiteId(@PathVariable("title") String title,
                                                               @PathVariable("siteId") Long siteId,
                                                               @RequestParam(name = "page", defaultValue = "0") int page,
                                                               @RequestParam(name = "size", defaultValue = "10") int size) {
        return productService.findAllProductsByTitleAndSiteIdInPage(PageRequest.of(page, size), title, siteId);
    }

    @GetMapping("/product/findCostProgress/{siteId}")
    public PageDto<ProductCostChangeDto> findCostProgress(@PathVariable("siteId") Long siteId,
                                                          @RequestParam(name = "page", defaultValue = "0") int page,
                                                          @RequestParam(name = "size", defaultValue = "10") int size) {
        return productService.getPriceHistoryBySiteIdInPage(PageRequest.of(page, size), siteId);
    }

    @GetMapping("/product/findNew/{siteId}")
    public PageDto<ProductDto> findNewProducts(@PathVariable("siteId") Long siteId,
                                               @RequestParam(name = "page", defaultValue = "0") int page,
                                               @RequestParam(name = "size", defaultValue = "10") int size) {
        return productService.rightJoinProductsBySiteId(PageRequest.of(page, size), siteId);
    }

    @GetMapping("/product/findOld/{siteId}")
    public PageDto<ProductDto> findOldProducts(@PathVariable("siteId") Long siteId,
                                               @RequestParam(name = "page", defaultValue = "0") int page,
                                               @RequestParam(name = "size", defaultValue = "10") int size) {
        return productService.leftJoinProductsBySiteId(PageRequest.of(page, size), siteId);
    }
}
