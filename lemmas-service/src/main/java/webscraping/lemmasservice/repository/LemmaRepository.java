package webscraping.lemmasservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import webscraping.lemmasservice.model.Lemma;

import java.util.ArrayList;
import java.util.List;

public interface LemmaRepository extends JpaRepository<Lemma, Long> {
    Lemma findByLemma(String lemma);

    List<Lemma> findByLemmaIn(ArrayList<String> strings);

    List<Lemma> findByLemmaInAndSiteId(ArrayList<String> strings, Long siteId);

    void deleteAllBySiteId(Long siteId);
}
