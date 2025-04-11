package webscraping.parserservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import webscraping.parserservice.dto.S3KeyDto;
import webscraping.parserservice.model.Blog;
import webscraping.parserservice.util.BlogParser;
import webscraping.parserservice.util.S3KeyParser;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlogService {

    public Blog buildBlog(String path, String html, String s3key) {
        S3KeyDto s3KeyDto = S3KeyParser.getS3KeyDto(s3key);
        Blog blog = new Blog();

        try {
            blog.setId(UUID.randomUUID());
            blog.setBlogText(BlogParser.getContent(html));

            if(blog.getBlogText() == null) {
                return null;
            }

            blog.setPath(path);
            blog.setKeyWords(BlogParser.findKeyWord(html));
            blog.setParseTime(s3KeyDto.getDate());
            blog.setSiteId(s3KeyDto.getSiteId());

            String baseUrl = BlogParser.getBaseUrl(path);

            blog.setImages(BlogParser.getImagesFromArticle(html, baseUrl));
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return blog;
    }
}
