package webscraping.crawlerservice.repository;

import jakarta.transaction.Transactional;
import org.hibernate.annotations.processing.SQL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import webscraping.crawlerservice.model.Page;
import webscraping.crawlerservice.model.Site;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PageRepository extends JpaRepository<Page, UUID> {

    Optional<Page> findByPath(String path);

    List<Page> findBySite(Site site);

    @Transactional
    @Modifying
    @Query("DELETE FROM pages WHERE site IS NULL")
    void deleteBySiteIsNull();

}
