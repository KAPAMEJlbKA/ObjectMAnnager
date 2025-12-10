package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.config.FileStorageProperties;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SiteAttachmentStorageService {

    private final Path rootLocation;

    public SiteAttachmentStorageService(FileStorageProperties properties) {
        this.rootLocation = Path.of(properties.getUploadDir())
                .resolve("site-attachments")
                .toAbsolutePath()
                .normalize();
        init();
    }

    private void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to create site attachments directory", ex);
        }
    }

    public StoredAttachmentFile store(Long siteId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "file";
        }
        originalFilename = StringUtils.cleanPath(originalFilename);
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.') + 1;
        if (dotIndex > 0 && dotIndex < originalFilename.length()) {
            extension = originalFilename.substring(dotIndex - 1);
        }
        String storedFilename = UUID.randomUUID() + extension;

        Path destinationDirectory = rootLocation.resolve(siteId.toString());
        try {
            Files.createDirectories(destinationDirectory);
            Path destinationFile = destinationDirectory.resolve(storedFilename).normalize();
            if (!destinationFile.startsWith(destinationDirectory)) {
                throw new IllegalStateException("Cannot store file outside designated directory");
            }
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            String contentType = file.getContentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = Files.probeContentType(destinationFile);
            }
            long size = Files.size(destinationFile);
            return new StoredAttachmentFile(storedFilename, originalFilename, size, contentType);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store attachment", ex);
        }
    }

    public Resource loadAsResource(Long siteId, String storageKey) {
        Path filePath = load(siteId, storageKey);
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("Failed to read attachment", ex);
        }
        throw new IllegalStateException("Attachment not found: " + storageKey);
    }

    public void delete(Long siteId, String storageKey) {
        Path filePath = load(siteId, storageKey);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to delete attachment", ex);
        }
    }

    private Path load(Long siteId, String storageKey) {
        if (storageKey == null) {
            throw new IllegalArgumentException("Storage key is required");
        }
        Path destinationDirectory = rootLocation.resolve(siteId.toString());
        Path filePath = destinationDirectory.resolve(storageKey).normalize();
        if (!filePath.startsWith(destinationDirectory)) {
            throw new IllegalStateException("Attempt to access file outside of site directory");
        }
        return filePath;
    }

    public record StoredAttachmentFile(String storageKey, String originalFilename, long size, String contentType) {}
}
