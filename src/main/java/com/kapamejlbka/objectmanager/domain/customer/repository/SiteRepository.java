package com.kapamejlbka.objectmanager.domain.customer.repository;

import com.kapamejlbka.objectmanager.domain.customer.Site;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteRepository extends JpaRepository<Site, Long> {

    List<Site> findByCustomerId(Long customerId);
}
