package webscraping.entityvaultservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import webscraping.entityvaultservice.model.Blog;

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

}
