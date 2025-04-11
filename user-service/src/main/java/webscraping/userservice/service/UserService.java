package webscraping.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import webscraping.userservice.model.User;
import webscraping.userservice.repository.UserRepository;
import webscraping.userservice.util.UserContext;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public User save(User user) {
        return userRepository.save(user);
    }

    public User findById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    public User findThisAccount() {
        return findById(UserContext.getUserId());
    }

    public User update(User user) {
        User existingUser = findById(UserContext.getUserId());

        if (existingUser != null) {
            existingUser.setName(user.getName());
            existingUser.setEmail(user.getEmail());
            existingUser.setPhoto(user.getPhoto());
            existingUser.setCity(user.getCity());
            existingUser.setCountry(user.getCountry());
            existingUser.setPhone(user.getPhone());
            return save(existingUser);
        }

        return save(user);
    }

    public List<User> findAllByIds(List<UUID> id) {
        return userRepository.findAllById(id);
    }
}

