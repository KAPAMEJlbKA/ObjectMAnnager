package com.kapamejlbka.objectmannage.repository;

import com.kapamejlbka.objectmannage.domain.Site;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SiteRepository extends JpaRepository<Site, UUID> {

    Page<Site> findByNameContainingIgnoreCaseOrCityContainingIgnoreCaseOrAddressContainingIgnoreCase(
            String name,
            String city,
            String address,
            Pageable pageable
    );
}
