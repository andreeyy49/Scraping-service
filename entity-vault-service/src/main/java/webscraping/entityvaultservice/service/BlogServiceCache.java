package webscraping.entityvaultservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import webscraping.entityvaultservice.dto.BlogDto;
import webscraping.entityvaultservice.model.Blog;
import webscraping.entityvaultservice.repository.BlogRepository;
import webscraping.entityvaultservice.util.DateUtil;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlogServiceCache {

    private final BlogRepository blogRepository;

    @Transactional
    @Cacheable(value = "findLatestBlogsByKeywords", key = "#keywords")
    public List<BlogDto> findLatestBlogsByKeywords(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return Collections.emptyList();
        }

        List<Blog> blogs = blogRepository.findAllByKeywords(keywords);

        if (blogs.isEmpty()) {
            log.warn("blogs in repo is empty");
            return Collections.emptyList();
        }

        Map<Long, List<Blog>> blogsGroupedBySiteId = blogs.stream()
                .collect(Collectors.groupingBy(Blog::getSiteId));

        Map<Long, Date> lastDates = findLastDates(blogsGroupedBySiteId);

        return blogsGroupedBySiteId.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .filter(blog -> {
                            Date lastDate = lastDates.get(entry.getKey());
                            return lastDate != null && blog.getParseTime().compareTo(lastDate) == 0;
                        }))
                .peek(blog -> Hibernate.initialize(blog.getImages()))
                .peek(blog -> Hibernate.initialize(blog.getKeyWords()))
                .map(this::blogToDto)
                .toList();
    }

    @Cacheable(value = "findLatestBlogsBySiteId", key = "#siteId")
    public List<BlogDto> findLatestBySiteId(Long siteId) {
        List<Blog> blogs = blogRepository.findAllBySiteId(siteId);

        Date lastDate = findLastDate(blogs);

        if (lastDate == null) {
            return new ArrayList<>();
        }

        return blogs.stream()
                .filter(blog -> blog.getParseTime().equals(lastDate))
                .map(this::blogToDto)
                .toList();
    }

    private Map<Long, Date> findLastDates(Map<Long, List<Blog>> blogs) {
        Map<Long, Date> lastDates = new HashMap<>();

        for (Map.Entry<Long, List<Blog>> entry : blogs.entrySet()) {
            lastDates.put(entry.getKey(), entry.getValue().stream()
                    .map(Blog::getParseTime)
                    .max(Date::compareTo)
                    .orElse(null));
        }

        return lastDates;
    }

    private Date findLastDate(List<Blog> blogs) {
        return blogs.stream()
                .map(Blog::getParseTime)
                .max(Date::compareTo)
                .orElse(null);
    }

    private BlogDto blogToDto(Blog blog) {
        log.info("Mapping blog: {}, images: {}", blog.getId(), blog.getImages());

        BlogDto dto = new BlogDto();

        dto.setBlogText(blog.getBlogText());
        dto.setPath(blog.getPath());
        dto.setParseTime(DateUtil.formatToLocalDate(blog.getParseTime()));
        dto.setSiteId(blog.getSiteId());
        dto.setKeyWords(blog.getKeyWords());
        dto.setImages(blog.getImages());

        return dto;
    }
}
