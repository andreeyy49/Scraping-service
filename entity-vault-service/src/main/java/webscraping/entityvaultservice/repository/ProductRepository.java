package webscraping.entityvaultservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import webscraping.entityvaultservice.model.Product;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findAllBySiteId(Long siteId);

    @Query(
            value = """
                    SELECT p.*\s
                    FROM product p
                    WHERE p.title ILIKE CONCAT('%', :title, '%');
                    """
            , nativeQuery = true)
    List<Product> findAllProductsByTitle(@Param("title") String title);

    @Query(value = """
            SELECT DISTINCT parse_time 
            FROM product 
            WHERE site_id = :siteId
            ORDER BY parse_time DESC 
            LIMIT 2
            """, nativeQuery = true)
    List<Date> findLastTwoUniqueDatesBySiteId(@Param("siteId") Long siteId);

    List<Product> findAllByParseTime(Date parseTime);
}
