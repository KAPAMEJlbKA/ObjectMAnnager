package com.kapamejlbka.objectmannage.repository;

import com.kapamejlbka.objectmannage.model.DeviceType;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceTypeRepository extends JpaRepository<DeviceType, UUID> {
}
