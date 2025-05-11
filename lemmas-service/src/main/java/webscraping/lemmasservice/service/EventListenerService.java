package webscraping.lemmasservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import webscraping.lemmasservice.dto.BlogDto;
import webscraping.lemmasservice.model.Lemma;
import webscraping.lemmasservice.util.LemmasParser;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventListenerService {

    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;
    private final LemmaService lemmaService;

    @KafkaListener(topics = "${app.kafka.saveBlogEntityTopic}", groupId = "${app.kafka.crawlerGroupId}")
    public void listenBlogTopic(ConsumerRecord<String, String> record) {
        try {
            BlogDto blog = objectMapper.readValue(record.value(), BlogDto.class);

            lemmaService.deleteAllBySiteId(blog.getSiteId());

            executorService.execute(()->{
                List<Lemma> lemmas = LemmasParser.lemmaParse(blog);
                lemmaService.saveAll(lemmas);
            });
        } catch (JsonProcessingException e) {
            log.error("Blog parse error: {}", e.getMessage());
        }
    }
}
