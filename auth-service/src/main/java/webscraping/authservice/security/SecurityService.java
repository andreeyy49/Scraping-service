package webscraping.authservice.security;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import webscraping.authservice.dto.request.LoginRequest;
import webscraping.authservice.dto.request.RegistrationRequest;
import webscraping.authservice.dto.response.LoginResponse;
import webscraping.authservice.exception.RefreshTokenException;
import webscraping.authservice.model.RefreshToken;
import webscraping.authservice.model.User;
import webscraping.authservice.repository.UserRepository;
import webscraping.authservice.security.jjwt.JwtUtils;
import webscraping.authservice.service.RefreshTokenService;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final AuthenticationManager authenticationManager;

    private final JwtUtils jwtUtils;

    private final RefreshTokenService refreshTokenService;

    private final UserRepository userAuthRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${app.salt}")
    private String salt;

    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        if (loginRequest.getEmail().isBlank() || loginRequest.getPassword().isBlank()) {
            throw new IllegalArgumentException("Email and password must not be null");
        }

        try {
            Authentication authentication = authenticateWithPassword(loginRequest.getEmail(), loginRequest.getPassword());

            AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
            return LoginResponse.builder()
                    .accessToken(jwtUtils.generateTokenFromUserId(userDetails.getId()))
                    .refreshToken(refreshToken.getToken())
                    .build();
        } catch (IllegalArgumentException e) {
            Authentication authentication = authenticateWithPassword(loginRequest.getEmail(), loginRequest.getPassword());

            AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
            return LoginResponse.builder()
                    .accessToken(jwtUtils.generateTokenFromUserId(userDetails.getId()))
                    .refreshToken(refreshToken.getToken())
                    .build();
        }
    }

    public Authentication authenticateWithPassword(String email, String password) {
        User userAuth = userAuthRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userAuth.getEmail(), salt + password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return authentication;
    }

    public User register(RegistrationRequest request) {
        var user = User.builder()
                .username(request.getUserName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(salt + request.getPassword1()))
                .id(UuidCreator.getTimeOrdered())
                .build();

        return userAuthRepository.save(user);
    }

    public LoginResponse refreshToken(String requestRefreshToken) {
        return refreshTokenService.findByRefreshToken(requestRefreshToken)
                .map(refreshTokenService::checkRefreshToken)
                .map(RefreshToken::getUserId)
                .map(userId -> {
                    User tokenOwner = userAuthRepository.findById(userId).orElseThrow(() ->
                            new RefreshTokenException("Exception trying to get token for userId: " + userId));

                    return new LoginResponse(LocalDateTime.now(), jwtUtils.generateTokenFromUserId(tokenOwner.getId()), refreshTokenService.createRefreshToken(userId).getToken());
                }).orElseThrow(() -> new RefreshTokenException(requestRefreshToken, "Refresh token not found"));
    }

    public void logout() {
        var currentPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (currentPrincipal instanceof AppUserDetails userDetails) {
            UUID userId = userDetails.getId();

            refreshTokenService.deleteByUserId(userId);
        }
    }

}

