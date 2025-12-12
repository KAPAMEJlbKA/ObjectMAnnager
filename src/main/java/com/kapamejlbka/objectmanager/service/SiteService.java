package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.customer.Customer;
import com.kapamejlbka.objectmanager.domain.customer.Site;
import com.kapamejlbka.objectmanager.domain.customer.dto.SiteCreateRequest;
import com.kapamejlbka.objectmanager.domain.customer.dto.SiteUpdateRequest;
import com.kapamejlbka.objectmanager.domain.customer.repository.CustomerRepository;
import com.kapamejlbka.objectmanager.domain.customer.repository.SiteRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SiteService {

    private final SiteRepository siteRepository;
    private final CustomerRepository customerRepository;

    public SiteService(SiteRepository siteRepository, CustomerRepository customerRepository) {
        this.siteRepository = siteRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Site create(Long customerId, SiteCreateRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Site data is required");
        }
        Site site = new Site();
        site.setCustomer(getCustomerById(customerId));
        applyDto(site, dto);
        LocalDateTime now = LocalDateTime.now();
        site.setCreatedAt(now);
        site.setUpdatedAt(now);
        return siteRepository.save(site);
    }

    @Transactional
    public Site update(Long id, SiteUpdateRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Site data is required");
        }
        Site site = getById(id);
        applyDto(site, dto);
        site.setUpdatedAt(LocalDateTime.now());
        return siteRepository.save(site);
    }

    public Site getById(Long id) {
        return siteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Site not found: " + id));
    }

    public List<Site> findByCustomer(Long customerId) {
        return siteRepository.findByCustomerId(customerId);
    }

    public long countByCustomer(Long customerId) {
        return siteRepository.countByCustomerId(customerId);
    }

    @Transactional
    public void delete(Long id) {
        Site site = getById(id);
        siteRepository.delete(site);
    }

    private void applyDto(Site site, SiteCreateRequest dto) {
        String name = normalize(dto.getName());
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Site name is required");
        }
        site.setName(name);
        site.setDescription(normalize(dto.getDescription()));
        site.setLatitude(dto.getLatitude());
        site.setLongitude(dto.getLongitude());
        site.setFullAddress(normalize(dto.getFullAddress()));
        site.setContactName(normalize(dto.getContactName()));
        site.setContactPosition(normalize(dto.getContactPosition()));
        site.setContactPhone(normalize(dto.getContactPhone()));
        site.setContactEmail(normalize(dto.getContactEmail()));
        site.setUseCustomerContact(dto.isUseCustomerContact());
    }

    private Customer getCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
