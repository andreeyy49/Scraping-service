package webscraping.crawlerservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import webscraping.crawlerservice.model.Site;

import java.util.Optional;

public interface SiteRepository extends JpaRepository<Site, Long> {
    Optional<Site> findByUrl(String url);

    void deleteByUrl(String url);
}
