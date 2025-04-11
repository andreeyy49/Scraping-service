package webscraping.crawlerservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.publishPageTopic}")
    private String topic;

    public void sendPagesToTopic(String s3Key) {
            kafkaTemplate.send(topic, s3Key);
            log.info("Сообщение отправлено");
    }
}
