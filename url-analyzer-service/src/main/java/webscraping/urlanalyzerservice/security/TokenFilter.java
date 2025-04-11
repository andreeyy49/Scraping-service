package webscraping.urlanalyzerservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import webscraping.urlanalyzerservice.client.AuthClient;
import webscraping.urlanalyzerservice.dto.ErrorResponse;
import webscraping.urlanalyzerservice.exception.InvalidTokenException;
import webscraping.urlanalyzerservice.util.UserContext;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenFilter extends OncePerRequestFilter {

    private final AuthClient authClient;

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        log.info("Received token: {}", token);

        try {
            // Проверка токена
            boolean isValid = authClient.validateToken(token);
            log.info("Token valid: {}", isValid);

            if (!isValid) {
                log.warn("Invalid token detected, rejecting request.");
                throw new InvalidTokenException("Invalid token");
            }

            // Извлечение userId
            String userId = getUserId(token);
            log.info("Extracted userId: {}", userId);

            // Установка контекста пользователя
            UserContext.setUserId(UUID.fromString(userId));
            UserContext.setToken(token);

            // Создание аутентификации
            Authentication authentication = getAuthentication(userId);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Передаем запрос дальше в цепочку
            filterChain.doFilter(request, response);
        } catch (InvalidTokenException e) {
            log.error("Token validation failed: {}", e.getMessage());

            // Отправка ответа об ошибке
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            response.getWriter().flush();
        } finally {
            // Очистка контекста пользователя
            UserContext.clear();
            log.info("UserContext cleared.");
        }
    }

    private Authentication getAuthentication(String userId) {
        return new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
    }

    private String getUserId(String token) {
        try {
            token = token.substring(7); // Убираем "Bearer " из токена
            return SignedJWT.parse(token).getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            log.error("Error parsing token: {}", e.getMessage());
            throw new InvalidTokenException("Token doesn't have payload");
        }
    }
}
