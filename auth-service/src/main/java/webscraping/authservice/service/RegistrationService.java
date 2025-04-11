package webscraping.authservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import webscraping.authservice.dto.event.UserRegistrationEvent;
import webscraping.authservice.dto.request.RegistrationRequest;
import webscraping.authservice.exception.AlreadyExistsException;
import webscraping.authservice.exception.DifferentPasswordsException;
import webscraping.authservice.model.User;
import webscraping.authservice.security.SecurityService;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final SecurityService securityService;

    private final UserService userAuthService;

    @Value("${app.kafka.kafkaEventTopic}")
    private String topicName;

    private final KafkaTemplate<String, String> userRegisteredEvent;

    private final ObjectMapper objectMapper;

    public void registerUser(RegistrationRequest request) {
        if (userAuthService.existsByUsername(request.getUserName())) {
            throw new AlreadyExistsException("Пользователь уже существует");
        }
        if (userAuthService.existsByEmail(request.getEmail())) {
            throw new AlreadyExistsException("Электронная почта уже зарегистрирована");
        }
        if (!request.getPassword1().equals(request.getPassword2())) {
            throw new DifferentPasswordsException("Пароли должны совпадать");
        }
        UserRegistrationEvent event = UserRegistrationEvent.builder()
                .email(request.getEmail())
                .userName(request.getUserName())
                .build();
        String mappedEvent;

        User user = securityService.register(request);

        event.setId(String.valueOf(user.getId()));

        try {
            mappedEvent = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        userRegisteredEvent.send(topicName, mappedEvent);
    }
}
