package webscraping.authservice.security.jjwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.tokenExpiration}")
    private Duration tokenExpiration;

    public String generateTokenFromUserId(UUID userId) {
        log.debug("Генерация JWT для userId: {}", userId);

        SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + tokenExpiration.toMillis()))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        log.info("JWT успешно сгенерирован для userId: {}", userId);
        return token;
    }

    public String getEmail(String token) {
        log.debug("Извлечение email из JWT");

        try {
            SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            String email = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();

            log.info("Email успешно извлечен из JWT: {}", email);
            return email;
        } catch (JwtException e) {
            log.warn("Ошибка при извлечении email из JWT: {}", e.getMessage());
            throw e;
        }
    }

    public boolean validate(String authToken) {
        log.debug("Валидация JWT");

        try {
            SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(authToken);

            log.info("JWT успешно прошел валидацию");
            return true;
        } catch (SecurityException e) {
            log.warn("Ошибка безопасности при валидации JWT: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Некорректный JWT: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("JWT истек: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Неподдерживаемый формат JWT: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT пуст или имеет недопустимый формат: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Неизвестная ошибка при валидации JWT", e);
        }

        return false;
    }
}
