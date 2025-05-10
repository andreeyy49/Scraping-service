package webscraping.entityvaultservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import webscraping.entityvaultservice.dto.BlogDto;
import webscraping.entityvaultservice.dto.BlogTextChangeDto;
import webscraping.entityvaultservice.model.Blog;
import webscraping.entityvaultservice.repository.BlogRepository;
import webscraping.entityvaultservice.util.DateUtil;
import webscraping.entityvaultservice.util.JoinEnum;

import java.util.*;
import java.util.function.Function;
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

    @Transactional
    @Cacheable(value = "findLatestBlogsBySiteId", key = "#siteId")
    public List<BlogDto> findLatestBySiteId(Long siteId) {
        List<Blog> blogs = blogRepository.findAllBySiteId(siteId);

        Date lastDate = findLastDate(blogs);

        if (lastDate == null) {
            return new ArrayList<>();
        }

        return blogs.stream()
                .filter(blog -> blog.getParseTime().equals(lastDate))
                .peek(blog -> Hibernate.initialize(blog.getImages()))
                .peek(blog -> Hibernate.initialize(blog.getKeyWords()))
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

    @Cacheable(value = "findChangedBlogs", key = "#siteId")
    public List<BlogTextChangeDto> findChangedBlogs(Long siteId) {
        List<Blog> allBlogs = blogRepository.findAllBySiteId(siteId);
        HashMap<String, Blog> uniqHashBlog = new HashMap<>();

        allBlogs.forEach(blog -> {
            List<Blog> blogsByPath = blogRepository.findAllByPath(blog.getPath());
            blogsByPath.forEach(b -> uniqHashBlog.putIfAbsent(b.getHash(), b));
        });

        return uniqHashBlog.values().stream()
                .collect(Collectors.groupingBy(Blog::getPath))
                .values().stream()
                .map(list -> {
                    List<BlogDto> dto = list.stream()
                            .sorted(Comparator.comparing(Blog::getParseTime))
                            .map(this::blogToDto)
                            .toList();

                    return new BlogTextChangeDto(dto);
                })
                .toList();
    }

    @Cacheable(value = "joinProductsBySiteId", key = "#siteId + '-' + #joinEnum")
    public List<BlogDto> joinProductsBySiteId(Long siteId, JoinEnum joinEnum) {
        List<Date> dates = blogRepository.findLastTwoUniqueDatesBySiteId(siteId);

        if (dates.isEmpty()) {
            return new ArrayList<>();
        }

        if (dates.size() == 1 && joinEnum.equals(JoinEnum.RIGHT)) {
            return blogRepository.findAllBySiteId(siteId).stream().map(this::blogToDto).toList();
        } else if (dates.size() < 2) {
            return new ArrayList<>();
        }

        List<Blog> lastBlogs = blogRepository.findAllByParseTime(dates.get(0));
        List<Blog> preLastBlogs = blogRepository.findAllByParseTime(dates.get(1));

        Function<Blog, String> key = k ->
                k.getPath().toLowerCase().trim() + "|" + k.getSiteId();

        if (joinEnum == JoinEnum.LEFT) {
            Set<String> newBlogs = lastBlogs.stream()
                    .map(key)
                    .collect(Collectors.toSet());

            return preLastBlogs.stream()
                    .filter(oldBlog -> !newBlogs.contains(key.apply(oldBlog)))
                    .map(this::blogToDto)
                    .collect(Collectors.toList());
        } else {
            Set<String> oldBlogs = preLastBlogs.stream()
                    .map(key)
                    .collect(Collectors.toSet());

            return lastBlogs.stream()
                    .filter(newBlog -> !oldBlogs.contains(key.apply(newBlog)))
                    .map(this::blogToDto)
                    .collect(Collectors.toList());
        }
    }

    private Date findLastDate(List<Blog> blogs) {
        return blogs.stream()
                .map(Blog::getParseTime)
                .max(Date::compareTo)
                .orElse(null);
    }

    private BlogDto blogToDto(Blog blog) {
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
