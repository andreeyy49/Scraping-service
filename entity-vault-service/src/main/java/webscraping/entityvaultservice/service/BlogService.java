package webscraping.entityvaultservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import webscraping.entityvaultservice.model.Blog;
import webscraping.entityvaultservice.repository.BlogRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlogService {

    private final BlogRepository blogRepository;

    public Blog save(Blog blog) {
        return blogRepository.save(blog);
    }
}
