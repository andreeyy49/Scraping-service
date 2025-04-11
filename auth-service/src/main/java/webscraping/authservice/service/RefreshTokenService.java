package webscraping.authservice.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import webscraping.authservice.exception.RefreshTokenException;
import webscraping.authservice.model.RefreshToken;
import webscraping.authservice.repository.RefreshTokenRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${app.jwt.refreshTokenExpiration}")
    private Duration refreshTokenExpiration;

    private final RefreshTokenRepository repository;

    public RefreshTokenService(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    public Optional<RefreshToken> findByRefreshToken(String token) {
        return repository.findByToken(token);
    }

    public RefreshToken createRefreshToken(UUID userId) {
        var refreshToken = RefreshToken.builder()
                .userId(userId)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration.toMillis()))
                .token(UUID.randomUUID().toString())
                .build();
        return repository.save(refreshToken);
    }
    public RefreshToken checkRefreshToken(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            repository.delete(token);
            throw new RefreshTokenException(token.getToken(), "Refresh token was expired. Repeat sign in action!");
        }
        return token;
    }
    public void deleteByUserId(UUID userId) {
        repository.deleteByUserId(userId);
    }

}
