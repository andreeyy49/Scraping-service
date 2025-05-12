package webscraping.lemmasservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import webscraping.lemmasservice.model.Lemma;

import java.util.List;
import java.util.Optional;

public interface LemmaRepository extends JpaRepository<Lemma, Long> {

    Optional<Lemma> findByLemmaAndSiteId(String lemmaValue, Long siteId);

    @Modifying
    @Query(nativeQuery = true,
            value = "INSERT INTO lemmas (site_id, lemma, frequency) " +
                    "VALUES (:siteId, :lemma, 1) " +
                    "ON CONFLICT (site_id, lemma) " +  // Указываем колонки уникального индекса
                    "DO UPDATE SET frequency = lemmas.frequency + 1")
    void upsertLemma(@Param("siteId") Long siteId, @Param("lemma") String lemma);

    @Query("SELECT DISTINCT l FROM Lemma l LEFT JOIN FETCH l.indexes WHERE l.lemma IN :lemmas")
    List<Lemma> findByLemmaInWithIndexes(@Param("lemmas") List<String> lemmas);

    @Query("SELECT DISTINCT l FROM Lemma l LEFT JOIN FETCH l.indexes WHERE l.lemma IN :lemmas AND l.siteId = :siteId")
    List<Lemma> findByLemmaInAndSiteIdWithIndexes(
            @Param("lemmas") List<String> lemmas,
            @Param("siteId") Long siteId);
}
