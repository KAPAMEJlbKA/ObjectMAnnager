package com.kapamejlbka.objectmannage.repository;

import com.kapamejlbka.objectmannage.model.StoredFile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {
}
