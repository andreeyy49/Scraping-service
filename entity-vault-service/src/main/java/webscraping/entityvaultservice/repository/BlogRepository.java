package webscraping.entityvaultservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import webscraping.entityvaultservice.dto.BlogDto;
import webscraping.entityvaultservice.dto.PageDto;
import webscraping.entityvaultservice.model.Blog;
import webscraping.entityvaultservice.model.Product;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface BlogRepository extends JpaRepository<Blog, UUID> {

    List<Blog> findAllBySiteId(Long siteId);

    @Query(value = """
            SELECT b.*
            FROM blog b
            JOIN blog_key_words k ON b.id = k.blog_id
            WHERE k.key_word IN (:keywords)
            """, nativeQuery = true)
    List<Blog> findAllByKeywords(@Param("keywords") List<String> keywords);

    @Query(value = "SELECT key_word, COUNT(*) FROM blog_key_words GROUP BY key_word", nativeQuery = true)
    List<Object[]> findKeywordsWithCount();

    List<Blog> findAllByPath(String path);

    @Query(value = """
            SELECT DISTINCT parse_time 
            FROM blog 
            WHERE site_id = :siteId
            ORDER BY parse_time DESC 
            LIMIT 2
            """, nativeQuery = true)
    List<Date> findLastTwoUniqueDatesBySiteId(@Param("siteId") Long siteId);

    List<Blog> findAllByParseTime(Date date);

    List<Blog> findByIdIn(List<UUID> blogIds);
}
