package com.kapamejlbka.objectmannage.repository;

import com.kapamejlbka.objectmannage.model.InstallationMaterial;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstallationMaterialRepository extends JpaRepository<InstallationMaterial, UUID> {
}
