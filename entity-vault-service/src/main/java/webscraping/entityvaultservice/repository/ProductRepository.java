package webscraping.entityvaultservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import webscraping.entityvaultservice.model.Product;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
}
