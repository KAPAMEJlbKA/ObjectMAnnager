package com.kapamejlbka.objectmanager.legacy.objectmannage.service;

import com.kapamejlbka.objectmanager.legacy.objectmannage.domain.Site;
import com.kapamejlbka.objectmanager.legacy.objectmannage.repository.SiteRepository;
import com.kapamejlbka.objectmanager.legacy.objectmannage.web.form.SiteForm;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SiteService {

    private final SiteRepository siteRepository;

    public SiteService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    public Site create(SiteForm form) {
        Site site = new Site();
        form.applyTo(site);
        return siteRepository.save(site);
    }

    public Site update(UUID id, SiteForm form) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Site not found: " + id));
        form.applyTo(site);
        return siteRepository.save(site);
    }

    @Transactional(readOnly = true)
    public Page<Site> findPage(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return siteRepository.findAll(pageable);
        }
        String normalized = query.trim();
        return siteRepository.findByNameContainingIgnoreCaseOrCityContainingIgnoreCaseOrAddressContainingIgnoreCase(
                normalized,
                normalized,
                normalized,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Site get(UUID id) {
        return siteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Site not found: " + id));
    }

    public void delete(UUID id) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Site not found: " + id));
        siteRepository.delete(site);
    }

    @Transactional(readOnly = true)
    public List<Site> findAllSorted() {
        return siteRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }
}
