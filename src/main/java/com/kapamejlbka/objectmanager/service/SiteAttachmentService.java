package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.customer.Site;
import com.kapamejlbka.objectmanager.domain.customer.SiteAttachment;
import com.kapamejlbka.objectmanager.domain.customer.dto.SiteAttachmentCreateRequest;
import com.kapamejlbka.objectmanager.domain.customer.repository.SiteAttachmentRepository;
import com.kapamejlbka.objectmanager.domain.customer.repository.SiteRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SiteAttachmentService {

    private final SiteAttachmentRepository siteAttachmentRepository;
    private final SiteRepository siteRepository;

    public SiteAttachmentService(
            SiteAttachmentRepository siteAttachmentRepository, SiteRepository siteRepository) {
        this.siteAttachmentRepository = siteAttachmentRepository;
        this.siteRepository = siteRepository;
    }

    @Transactional
    public SiteAttachment addAttachment(Long siteId, SiteAttachmentCreateRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Attachment data is required");
        }
        Site site = getSiteById(siteId);
        SiteAttachment attachment = new SiteAttachment();
        attachment.setSite(site);
        applyDto(attachment, dto);
        attachment.setUploadedAt(LocalDateTime.now());
        return siteAttachmentRepository.save(attachment);
    }

    public List<SiteAttachment> listAttachments(Long siteId) {
        return siteAttachmentRepository.findBySiteId(siteId);
    }

    @Transactional
    public void deleteAttachment(Long attachmentId) {
        SiteAttachment attachment = siteAttachmentRepository
                .findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found: " + attachmentId));
        siteAttachmentRepository.delete(attachment);
    }

    private void applyDto(SiteAttachment attachment, SiteAttachmentCreateRequest dto) {
        String fileName = normalize(dto.getFileName());
        if (!StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("Attachment file name is required");
        }
        String storageKey = normalize(dto.getStorageKey());
        if (!StringUtils.hasText(storageKey)) {
            throw new IllegalArgumentException("Attachment storage key is required");
        }
        attachment.setFileName(fileName);
        attachment.setStorageKey(storageKey);
        attachment.setContentType(normalize(dto.getContentType()));
        attachment.setSize(dto.getSize());
        attachment.setAttachmentType(normalize(dto.getAttachmentType()));
    }

    private Site getSiteById(Long siteId) {
        return siteRepository
                .findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Site not found: " + siteId));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
