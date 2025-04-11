package webscraping.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import webscraping.userservice.model.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

}
