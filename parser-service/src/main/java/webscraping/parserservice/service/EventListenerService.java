package webscraping.parserservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import webscraping.parserservice.dto.S3PagesDto;
import webscraping.parserservice.enums.SiteDataType;
import webscraping.parserservice.model.Blog;
import webscraping.parserservice.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventListenerService {

    private final S3Service s3Service;
    private final ExecutorService executorService;
    private final EComersService ecomersService;
    private final BlogService blogService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.saveProductEntityTopic}")
    private String saveProductTopic;

    @Value("${app.kafka.saveBlogEntityTopic}")
    private String saveBlogTopic;

    @KafkaListener(topics = "${app.kafka.publishPageTopic}", groupId = "${app.kafka.crawlerGroupId}")
    public void listenPage(ConsumerRecord<String, String> record) {
        log.info("Received record: {}", record);

        S3PagesDto s3PagesDto = s3Service.downloadHtml(record.value());

        if (s3PagesDto == null) {
            log.warn("S3 object is null");
            return;
        }

        List<Future<Blog>> blogFutures = new ArrayList<>();
        List<Future<Product>> productFutures = new ArrayList<>();

        log.info("Getting s3 object from kafka");
        try {
            if (s3PagesDto.getSiteDataType().equals(SiteDataType.E_COMMERCE)) {
                for (Map.Entry<String, String> entry : s3PagesDto.getPages().entrySet()) {
                    productFutures.add(executorService.submit(() -> ecomersService.buildProduct(entry.getKey(), entry.getValue(), record.value())));
                }

                for (Future<Product> productFuture : productFutures) {
                    ObjectMapper mapper = new ObjectMapper();
                    Product product = productFuture.get();
                    if(product == null) continue;
                    String productEvent = mapper.writeValueAsString(product);
                    kafkaTemplate.send(saveProductTopic, productEvent);
                }

            } else if (s3PagesDto.getSiteDataType().equals(SiteDataType.BLOG)) {

                for (Map.Entry<String, String> entry : s3PagesDto.getPages().entrySet()) {
                    blogFutures.add(executorService.submit(() -> blogService.buildBlog(entry.getKey(), entry.getValue(), record.value())));
                }

                for (Future<Blog> blogFuture : blogFutures) {
                    ObjectMapper mapper = new ObjectMapper();
                    Blog blog = blogFuture.get();
                    if(blog == null) continue;
                    String blogEvent = mapper.writeValueAsString(blog);
                    kafkaTemplate.send(saveBlogTopic, blogEvent);
                }

            }
        } catch (JsonProcessingException | InterruptedException | ExecutionException e) {
            log.error("Exception: {}", e.getMessage());
        }

        log.info("Kafka listener finished");
    }
}