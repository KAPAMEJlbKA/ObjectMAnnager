package com.kapamejlbka.objectmanager.web;

import com.kapamejlbka.objectmanager.domain.customer.SiteAttachment;
import com.kapamejlbka.objectmanager.domain.customer.dto.SiteAttachmentCreateRequest;
import com.kapamejlbka.objectmanager.service.SiteAttachmentService;
import com.kapamejlbka.objectmanager.service.SiteAttachmentStorageService;
import com.kapamejlbka.objectmanager.service.SiteAttachmentStorageService.StoredAttachmentFile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class SiteAttachmentController {

    private final SiteAttachmentService siteAttachmentService;
    private final SiteAttachmentStorageService storageService;

    public SiteAttachmentController(
            SiteAttachmentService siteAttachmentService, SiteAttachmentStorageService storageService) {
        this.siteAttachmentService = siteAttachmentService;
        this.storageService = storageService;
    }

    @PostMapping("/sites/{siteId}/attachments")
    public RedirectView upload(
            @PathVariable("siteId") Long siteId,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        try {
            StoredAttachmentFile storedFile = storageService.store(siteId, file);
            SiteAttachmentCreateRequest request = new SiteAttachmentCreateRequest();
            request.setFileName(storedFile.originalFilename());
            request.setStorageKey(storedFile.storageKey());
            request.setContentType(storedFile.contentType());
            request.setSize(storedFile.size());
            siteAttachmentService.addAttachment(siteId, request);
            redirectAttributes.addFlashAttribute("flashSuccess", "Файл загружен");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/sites/")
                .path(siteId.toString());
        return new RedirectView(builder.toUriString(), true);
    }

    @GetMapping("/attachments/{id}")
    public ResponseEntity<Resource> download(@PathVariable("id") Long id) {
        SiteAttachment attachment = getAttachment(id);
        Resource resource = storageService.loadAsResource(attachment.getSite().getId(), attachment.getStorageKey());
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (attachment.getContentType() != null && !attachment.getContentType().isBlank()) {
            mediaType = MediaType.parseMediaType(attachment.getContentType());
        }
        String contentDisposition = String.format(
                "inline; filename=\"%s\"", attachment.getFileName());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

    @GetMapping("/attachments/{id}/meta")
    public ResponseEntity<AttachmentMeta> meta(@PathVariable("id") Long id) {
        SiteAttachment attachment = getAttachment(id);
        return ResponseEntity.ok(
                new AttachmentMeta(attachment.getId(), attachment.getContentType(), attachment.getFileName()));
    }

    @DeleteMapping("/attachments/{id}")
    public RedirectView delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        SiteAttachment attachment = getAttachment(id);
        try {
            storageService.delete(attachment.getSite().getId(), attachment.getStorageKey());
            siteAttachmentService.deleteAttachment(id);
            redirectAttributes.addFlashAttribute("flashSuccess", "Файл удалён");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/sites/")
                .path(attachment.getSite().getId().toString());
        return new RedirectView(builder.toUriString(), true);
    }

    private SiteAttachment getAttachment(Long id) {
        try {
            return siteAttachmentService.getById(id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    public record AttachmentMeta(Long id, String contentType, String fileName) {}
}
