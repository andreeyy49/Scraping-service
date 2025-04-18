package webscraping.entityvaultservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import webscraping.entityvaultservice.model.Product;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query(
            value = """
                            SELECT pi.image_url
                            FROM product_images pi
                            JOIN product p ON pi.product_id = p.id
                            WHERE p.site_id = :siteId
                                AND p.parse_time = (
                                    SELECT MAX(parse_time) 
                                    FROM product 
                                    WHERE site_id = :siteId
                                )
                    """,
            nativeQuery = true
    )
    List<String> findLatestImageUrlsBySiteId(@Param("siteId") Long siteId);

    @Query(
            value = """
                    SELECT p.*\s
                    FROM product p
                    JOIN (
                        SELECT site_id, MAX(parse_time) AS max_time
                        FROM product
                        GROUP BY site_id
                    ) latest ON p.site_id = latest.site_id AND p.parse_time = latest.max_time
                    WHERE p.title ILIKE CONCAT('%', :title, '%');
                    """
            , nativeQuery = true)
    List<Product> findAllProductsByTitle(@Param("title") String title);


    @Query(value = """
            SELECT * FROM product
            WHERE site_id = :siteId
            AND product.title ILIKE :title;
            """
            , nativeQuery = true)
    List<Product> findAllProductsBySiteIdAndTitle(@Param("siteId") Long siteId, @Param("title") String title);
}
