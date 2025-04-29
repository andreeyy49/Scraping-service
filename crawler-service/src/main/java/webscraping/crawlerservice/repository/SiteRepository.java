package webscraping.crawlerservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import webscraping.crawlerservice.enums.SiteDataType;
import webscraping.crawlerservice.model.Site;

import java.util.List;
import java.util.Optional;

public interface SiteRepository extends JpaRepository<Site, Long> {
    Optional<Site> findByUrl(String url);

    void deleteByUrl(String url);

    List<Site> findAllByType(SiteDataType type);
}
