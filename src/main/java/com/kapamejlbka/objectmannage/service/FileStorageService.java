package com.kapamejlbka.objectmannage.service;

import com.kapamejlbka.objectmannage.config.FileStorageProperties;
import com.kapamejlbka.objectmannage.model.ManagedObject;
import com.kapamejlbka.objectmannage.model.StoredFile;
import com.kapamejlbka.objectmannage.repository.StoredFileRepository;
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
    private final StoredFileRepository storedFileRepository;

    public FileStorageService(FileStorageProperties properties, StoredFileRepository storedFileRepository) {
        this.rootLocation = Path.of(properties.getUploadDir()).toAbsolutePath().normalize();
        this.storedFileRepository = storedFileRepository;
        init();
    }

    private void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to create upload directory", ex);
        }
    }

    public StoredFile store(ManagedObject managedObject, MultipartFile file) {
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

        Path destinationDirectory = rootLocation.resolve(managedObject.getId().toString());
        try {
            Files.createDirectories(destinationDirectory);
            Path destinationFile = destinationDirectory.resolve(storedFilename);
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            StoredFile storedFile = new StoredFile(originalFilename, storedFilename, file.getSize(), LocalDateTime.now());
            storedFile.setManagedObject(managedObject);
            return storedFileRepository.save(storedFile);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store file", ex);
        }
    }

    public Path load(ManagedObject managedObject, String storedFilename) {
        return rootLocation.resolve(managedObject.getId().toString()).resolve(storedFilename).normalize();
    }

    public void deleteFile(StoredFile storedFile) {
        Path filePath = load(storedFile.getManagedObject(), storedFile.getStoredFilename());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to delete file", ex);
        }
        storedFileRepository.delete(storedFile);
    }
}
