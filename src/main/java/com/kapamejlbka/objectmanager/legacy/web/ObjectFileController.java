package com.kapamejlbka.objectmanager.legacy.web;

import com.kapamejlbka.objectmanager.domain.customer.StoredFile;
import com.kapamejlbka.objectmanager.domain.user.AppUser;
import com.kapamejlbka.objectmanager.service.FilePreviewService;
import com.kapamejlbka.objectmanager.service.ManagedObjectService;
import com.kapamejlbka.objectmanager.service.UserService;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ObjectFileController extends ObjectController {

    private final ManagedObjectService managedObjectService;
    private final UserService userService;
    private final FilePreviewService filePreviewService;

    public ObjectFileController(ManagedObjectService managedObjectService,
                                UserService userService,
                                FilePreviewService filePreviewService) {
        this.managedObjectService = managedObjectService;
        this.userService = userService;
        this.filePreviewService = filePreviewService;
    }

    @PostMapping("/{id}/files")
    public String uploadFile(@PathVariable UUID id,
                             @RequestParam("file") MultipartFile[] files,
                             Principal principal) {
        AppUser user = userService.getByUsername(principal.getName());
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                managedObjectService.addFile(id, file, user);
            }
        }
        return "redirect:/objects/" + id;
    }

    @GetMapping("/{objectId}/files/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID objectId,
                                                 @PathVariable UUID fileId,
                                                 @RequestParam(value = "download", defaultValue = "false") boolean download) {
        StoredFile file = managedObjectService.getFile(objectId, fileId);
        Resource resource = managedObjectService.loadFileResource(file);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (StringUtils.hasText(file.getContentType())) {
            try {
                mediaType = MediaType.parseMediaType(file.getContentType());
            } catch (IllegalArgumentException ignored) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
        }

        ContentDisposition disposition = (download ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(file.getOriginalFilename(), StandardCharsets.UTF_8)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDisposition(disposition);
        if (file.getSize() > 0) {
            headers.setContentLength(file.getSize());
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @GetMapping("/{objectId}/files/{fileId}/preview")
    public ResponseEntity<String> previewFile(@PathVariable UUID objectId,
                                              @PathVariable UUID fileId) {
        StoredFile file = managedObjectService.getFile(objectId, fileId);
        String previewHtml = filePreviewService.renderPreview(file);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(previewHtml);
    }
}
