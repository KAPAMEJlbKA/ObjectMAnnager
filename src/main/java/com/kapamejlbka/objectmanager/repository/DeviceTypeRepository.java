package com.kapamejlbka.objectmanager.repository;

import com.kapamejlbka.objectmanager.model.DeviceType;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceTypeRepository extends JpaRepository<DeviceType, UUID> {
}
