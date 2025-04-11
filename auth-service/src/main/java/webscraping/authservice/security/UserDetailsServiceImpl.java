package webscraping.authservice.security;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import webscraping.authservice.model.User;
import webscraping.authservice.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));

        return new AppUserDetails(user);
    }

    public UserDetails loadUserById(UUID id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return new AppUserDetails(user);
    }
}