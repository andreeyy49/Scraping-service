package webscraping.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import webscraping.userservice.dto.UserRegistrationEvent;
import webscraping.userservice.model.User;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventListener {

    private final ObjectMapper objectMapper;

    private final UserService userService;

    @KafkaListener(topics = "${app.kafka.kafkaEventTopic}", groupId = "${app.kafka.kafkaEventGroupId}")
    public void handleUserCreated(ConsumerRecord<String, String> record) {
        log.info("Received message: {}", record.value());
        try {
            UserRegistrationEvent event = objectMapper.readValue(record.value(), UserRegistrationEvent.class);

            User user = new User();
            user.setEmail(event.getEmail());
            user.setName(event.getUserName());
            user.setId(UUID.fromString(event.getId()));

            userService.save(user);

            log.info("Получено событие: Пользователь {} создан (email: {})", event.getUserName(), event.getEmail());

            // Здесь можно обработать событие (сохранить в БД)
        } catch (JsonProcessingException e) {
            log.error("Ошибка десериализации Kafka-сообщения", e);
        }
    }
}
