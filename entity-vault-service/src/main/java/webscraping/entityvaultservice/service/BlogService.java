package webscraping.entityvaultservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import webscraping.entityvaultservice.model.Blog;
import webscraping.entityvaultservice.repository.BlogRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlogService {

    private final BlogRepository blogRepository;

    public Blog save(Blog blog) {
        return blogRepository.save(blog);
    }

    public List<String> findLatestImageUrlsBySiteId(Long siteId) {
        List<Blog> blogs = findLatestBySiteId(siteId);

        if(blogs.isEmpty()) return new ArrayList<>();

        return blogs.stream()
                .flatMap(blog -> blog.getImages().stream()).toList();
    }

    public List<Blog> findLatestBySiteId(Long siteId) {
        List<Blog> blogs = blogRepository.findAllBySiteId(siteId);

        Date lastDate = findLastDate(blogs);

        if(lastDate == null) {
            return new ArrayList<>();
        }

        return blogs.stream()
                .filter(blog -> blog.getParseTime().equals(lastDate))
                .toList();
    }

    public List<Blog> findLatestBlogsByKeywords(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return Collections.emptyList();
        }

        List<Blog> blogs = blogRepository.findAllByKeywords(keywords);

        if (blogs.isEmpty()) {
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
                .toList();
    }

    public List<Blog> findLatestBlogsByKeywordsAndSiteId(List<String> keywords, String siteId) {
        List<Blog> byKeywords = findLatestBlogsByKeywords(keywords);

        List<Blog> bySiteId = findLatestBySiteId(Long.parseLong(siteId));

        return byKeywords.stream()
                .filter(bySiteId::contains)
                .toList();
    }

    private Date findLastDate(List<Blog> blogs) {
        return blogs.stream()
                .map(Blog::getParseTime)
                .max(Date::compareTo)
                .orElse(null);
    }

    private Map<Long, Date> findLastDates(Map<Long, List<Blog>> blogs) {
        Map<Long, Date> lastDates = new HashMap<>();

        for(Map.Entry<Long, List<Blog>> entry : blogs.entrySet()) {
            lastDates.put(entry.getKey(), entry.getValue().stream()
                    .map(Blog::getParseTime)
                    .max(Date::compareTo)
                    .orElse(null));
        }

        return lastDates;
    }
}
