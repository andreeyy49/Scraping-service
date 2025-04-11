package webscraping.authservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import webscraping.authservice.dto.request.LoginRequest;
import webscraping.authservice.dto.response.LoginResponse;
import webscraping.authservice.security.SecurityService;
import webscraping.authservice.security.UserDetailsServiceImpl;
import webscraping.authservice.security.jjwt.JwtUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final SecurityService securityService;

    private final JwtUtils jwtUtils;

    private final UserDetailsServiceImpl userDetailsService;

    @Value("${app.kafka.kafkaValidateEventTopic}")
    private String topicName;

//    private final KafkaTemplate<String, String> validateTokenEvent;

    private final ObjectMapper objectMapper;

    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        return securityService.authenticateUser(loginRequest);
    }

    public LoginResponse refreshToken(String refreshToken) {
        return securityService.refreshToken(refreshToken);
    }

    public boolean validateToken(String headerAuth) {
        if (!StringUtils.hasText(headerAuth) || !headerAuth.startsWith("Bearer ")) {
            return false;
        }
        try {
            String token = headerAuth.substring(7);

            String uuid = jwtUtils.getEmail(token);

//            ValidateTokenEvent event = ValidateTokenEvent.builder().UUID(uuid).build();
//            validateTokenEvent.send(topicName, objectMapper.writeValueAsString(event));

            return userDetailsService.loadUserById(UUID.fromString(uuid)) != null;
        } catch (Exception e) {
            return false;
        }

    }
}
