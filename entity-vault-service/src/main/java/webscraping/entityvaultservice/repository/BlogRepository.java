package webscraping.entityvaultservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import webscraping.entityvaultservice.model.Blog;

import java.util.List;
import java.util.UUID;

public interface BlogRepository extends JpaRepository<Blog, UUID> {

    @Query(
            value = """
                    SELECT bi.image_url
                    FROM blog_images bi
                    JOIN blog b ON bi.blog_id = b.id
                    WHERE b.site_id = :siteId
                      AND b.parse_time = (
                          SELECT MAX(parse_time)
                          FROM blog
                          WHERE site_id = :siteId
                      )
                    """,
            nativeQuery = true
    )
    List<String> findLatestImageUrlsBySiteId(@Param("siteId") Long siteId);

}
