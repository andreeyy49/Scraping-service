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
            log.error("Parse error: {}", e.getMessage());
        }

    }

    @KafkaListener(topics = "${app.kafka.saveBlogEntityTopic}", groupId = "${app.kafka.crawlerGroupId}")
    public void listenBlogTopic(ConsumerRecord<String, String> record) {

        try {
            blogService.save(objectMapper.readValue(record.value(), Blog.class));
        } catch (JsonProcessingException e) {
            log.error("Parse error: {}", e.getMessage());
        }

    }
}
