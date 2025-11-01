package com.kapamejlbka.objectmanager.repository;

import com.kapamejlbka.objectmanager.model.InstallationMaterial;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstallationMaterialRepository extends JpaRepository<InstallationMaterial, UUID> {
}
