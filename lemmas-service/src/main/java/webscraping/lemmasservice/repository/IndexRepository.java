package webscraping.lemmasservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import webscraping.lemmasservice.model.Index;

public interface IndexRepository extends JpaRepository<Index, Long> {
}
