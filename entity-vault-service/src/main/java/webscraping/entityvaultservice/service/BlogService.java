package webscraping.entityvaultservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import webscraping.entityvaultservice.dto.BlogDto;
import webscraping.entityvaultservice.dto.PageDto;
import webscraping.entityvaultservice.model.Blog;
import webscraping.entityvaultservice.repository.BlogRepository;

import java.util.*;

import static webscraping.entityvaultservice.util.PageUtil.getPage;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlogService {

    private final BlogRepository blogRepository;

    private final BlogServiceCache blogServiceCache;

    public Blog save(Blog blog) {
        return blogRepository.save(blog);
    }

    public List<String> findLatestImageUrlsBySiteId(Long siteId) {
        List<BlogDto> blogs = blogServiceCache.findLatestBySiteId(siteId);

        if(blogs.isEmpty()) return new ArrayList<>();

        return blogs.stream()
                .flatMap(blog -> blog.getImages().stream()).toList();
    }

    public List<BlogDto> findLatestBlogsByKeywordsAndSiteId(List<String> keywords, Long siteId) {
        List<BlogDto> byKeywords = blogServiceCache.findLatestBlogsByKeywords(keywords);

        List<BlogDto> bySiteId = blogServiceCache.findLatestBySiteId(siteId);

        return byKeywords.stream()
                .filter(bySiteId::contains)
                .toList();
    }

    public PageDto<BlogDto> findLatestBlogsByKeywordsInPage(PageRequest request, List<String> keywords) {
        return getPage(request, blogServiceCache.findLatestBlogsByKeywords(keywords));
    }

    public PageDto<BlogDto> findLatestBlogsByKeywordsAndSiteIdInPage(PageRequest request, List<String> keywords, Long siteId) {
        return getPage(request, findLatestBlogsByKeywordsAndSiteId(keywords, siteId));
    }

    public List<String> findAllCategory() {
        List<Object[]> keysInDb = blogRepository.findKeywordsWithCount();

        Map<String, Long> map = new HashMap<>();

        for(Object[] objects: keysInDb) {
            String key = (String) objects[0];
            Long count = ((Number) objects[1]).longValue();

            map.put(key, count);
        }

        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .toList();
    }
}
