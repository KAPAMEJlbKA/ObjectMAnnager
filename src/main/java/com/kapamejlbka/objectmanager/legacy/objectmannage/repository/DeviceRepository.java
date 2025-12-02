package com.kapamejlbka.objectmanager.legacy.objectmannage.repository;

import com.kapamejlbka.objectmanager.legacy.objectmannage.domain.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID>, JpaSpecificationExecutor<Device> {
}
