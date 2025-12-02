package com.kapamejlbka.objectmanager.legacy.objectmannage.repository;

import com.kapamejlbka.objectmanager.legacy.objectmannage.domain.Site;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository("legacySiteRepository")
public interface SiteRepository extends JpaRepository<Site, UUID> {

    Page<Site> findByNameContainingIgnoreCaseOrCityContainingIgnoreCaseOrAddressContainingIgnoreCase(
            String name,
            String city,
            String address,
            Pageable pageable
    );
}
