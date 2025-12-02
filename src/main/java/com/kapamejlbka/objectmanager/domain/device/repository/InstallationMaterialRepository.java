package com.kapamejlbka.objectmanager.domain.device.repository;

import com.kapamejlbka.objectmanager.domain.device.InstallationMaterial;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstallationMaterialRepository extends JpaRepository<InstallationMaterial, UUID> {
}
