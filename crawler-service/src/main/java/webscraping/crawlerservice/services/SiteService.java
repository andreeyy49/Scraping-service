package webscraping.crawlerservice.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import webscraping.crawlerservice.dto.SiteDto;
import webscraping.crawlerservice.enums.SiteDataType;
import webscraping.crawlerservice.model.Site;
import webscraping.crawlerservice.repository.SiteRepository;
import webscraping.crawlerservice.util.BeanUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<SiteDto> getAllSitesStats() {
        List<SiteDto> sites = new ArrayList<>();
        sites.addAll(getEComers());
        sites.addAll(getBlogs());
        return sites;
    }

    public List<SiteDto> getEComers() {
        return getSitesByType(SiteDataType.E_COMMERCE);
    }

    public List<SiteDto> getBlogs() {
        return getSitesByType(SiteDataType.BLOG);
    }

    public List<SiteDto> getSitesByType(SiteDataType type) {
        List<Site> sites = repository.findAllByType(type);

        return sites.stream().map(site -> {
            SiteDto siteDto = new SiteDto();
            siteDto.setId(String.valueOf(site.getId()));
            siteDto.setUrl(site.getUrl());
            siteDto.setName(site.getName());
            siteDto.setStatus(site.getStatus());
            siteDto.setType(type.toString());
            siteDto.setPageCount(site.getPages().size());
            siteDto.setLastScraped(site.getStatusTime().toString());
            return siteDto;
        }).collect(Collectors.toList());
    }
}
