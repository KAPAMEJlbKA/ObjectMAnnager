package com.kapamejlbka.objectmanager.repository;

import com.kapamejlbka.objectmanager.model.StoredFile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {
    Optional<StoredFile> findByIdAndManagedObjectId(UUID id, UUID managedObjectId);
}
