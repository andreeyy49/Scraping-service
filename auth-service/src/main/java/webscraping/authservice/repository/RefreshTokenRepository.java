package webscraping.authservice.repository;

import org.springframework.data.repository.CrudRepository;
import webscraping.authservice.model.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String accessToken);

    void  deleteByUserId(UUID userId);

}
