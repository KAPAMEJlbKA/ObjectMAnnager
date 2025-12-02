package com.kapamejlbka.objectmanager.domain.customer.repository;

import com.kapamejlbka.objectmanager.domain.customer.SiteAttachment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteAttachmentRepository extends JpaRepository<SiteAttachment, Long> {

    List<SiteAttachment> findBySiteId(Long siteId);
}
