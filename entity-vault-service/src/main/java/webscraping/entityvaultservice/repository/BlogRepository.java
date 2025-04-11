package webscraping.entityvaultservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import webscraping.entityvaultservice.model.Blog;

import java.util.UUID;

public interface BlogRepository extends JpaRepository<Blog, UUID> {
}
