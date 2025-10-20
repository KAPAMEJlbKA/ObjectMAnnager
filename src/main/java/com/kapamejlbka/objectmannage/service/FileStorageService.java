package com.kapamejlbka.objectmannage.service;

import com.kapamejlbka.objectmannage.config.FileStorageProperties;
import com.kapamejlbka.objectmannage.model.StoredFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService(FileStorageProperties properties) {
        this.rootLocation = Path.of(properties.getUploadDir()).toAbsolutePath().normalize();
        init();
    }

    private void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to create upload directory", ex);
        }
    }

    public StoredFile store(UUID objectId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }
        UUID fileId = UUID.randomUUID();
        String storedFilename = fileId + extension;

        Path destinationDirectory = rootLocation.resolve(objectId.toString());
        try {
            Files.createDirectories(destinationDirectory);
            Path destinationFile = destinationDirectory.resolve(storedFilename);
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return new StoredFile(
                    fileId,
                    originalFilename,
                    storedFilename,
                    file.getSize(),
                    LocalDateTime.now()
            );
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store file", ex);
        }
    }

    public Path load(UUID objectId, String storedFilename) {
        return rootLocation.resolve(objectId.toString()).resolve(storedFilename).normalize();
    }
}
