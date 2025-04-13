package webscraping.crawlerservice.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import webscraping.crawlerservice.model.Site;
import webscraping.crawlerservice.repository.SiteRepository;
import webscraping.crawlerservice.util.BeanUtils;

import java.text.MessageFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SiteService {

    private final SiteRepository repository;

    public List<Site> findAll() {
        return repository.findAll();
    }

    public Site findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException(MessageFormat.format("Site with id:{0} not found", id)));
    }

    public Site findByUrl(String url) {
        return repository.findByUrl(url).orElse(null);
    }

    public Site save(Site site) {
        return repository.save(site);
    }

    public Site update(Site site) {
        Site oldSite = findById(site.getId());
        BeanUtils.copyNotNullProperties(site, oldSite);
        return repository.save(oldSite);
    }

    public void delete(Site site) {
        repository.delete(site);
    }

    public void deleteByUrl(String url) {
        repository.deleteByUrl(url);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
