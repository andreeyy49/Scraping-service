package webscraping.entityvaultservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import webscraping.entityvaultservice.dto.BlogDto;
import webscraping.entityvaultservice.dto.BlogTextChangeDto;
import webscraping.entityvaultservice.dto.PageDto;
import webscraping.entityvaultservice.model.Blog;
import webscraping.entityvaultservice.repository.BlogRepository;
import webscraping.entityvaultservice.util.JoinEnum;

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

        if (blogs.isEmpty()) return new ArrayList<>();

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

        for (Object[] objects : keysInDb) {
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

    public PageDto<BlogTextChangeDto> findChangedBlogsInPage(PageRequest request, Long siteId) {
        return getPage(request, blogServiceCache.findChangedBlogs(siteId));
    }

    public PageDto<BlogDto> rightJoinBlogsBySiteId(PageRequest request, Long siteId) {
        return getPage(request, blogServiceCache.joinProductsBySiteId(siteId, JoinEnum.RIGHT));
    }

    public PageDto<BlogDto> leftJoinBlogsBySiteId(PageRequest request, Long siteId) {
        return getPage(request, blogServiceCache.joinProductsBySiteId(siteId, JoinEnum.LEFT));
    }

    public PageDto<BlogDto> findByIds(PageRequest request, List<UUID> blogIds) {
        return getPage(request, blogServiceCache.findByIds(blogIds));
    }
}
