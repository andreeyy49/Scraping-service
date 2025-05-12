package webscraping.entityvaultservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import webscraping.entityvaultservice.model.Blog;
import webscraping.entityvaultservice.model.Product;
import webscraping.entityvaultservice.util.HashUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventListenerService {

    private final ObjectMapper objectMapper;
    private final BlogService blogService;
    private final ProductService productService;

    @KafkaListener(topics = "${app.kafka.saveProductEntityTopic}", groupId = "${app.kafka.crawlerGroupId}")
    public void listenProductTopic(ConsumerRecord<String, String> record) {

        try {
            productService.save(objectMapper.readValue(record.value(), Product.class));
        } catch (JsonProcessingException e) {
            log.error("Product parse error: {}", e.getMessage());
        }

    }

    @KafkaListener(topics = "${app.kafka.saveBlogEntityTopic}", groupId = "${app.kafka.crawlerGroupId}")
    public void listenBlogTopic(ConsumerRecord<String, String> record) {

        try {
            Blog blog = objectMapper.readValue(record.value(), Blog.class);
            log.info("blog id: {}", blog.getId());
            blog.setHash(HashUtil.getMd5Hash(blog.getBlogText()));
            blogService.save(blog);
        } catch (JsonProcessingException e) {
            log.error("Blog parse error: {}", e.getMessage());
        }

    }
}
