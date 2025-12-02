package com.kapamejlbka.objectmanager.domain.device.repository;

import com.kapamejlbka.objectmanager.domain.device.DeviceCableProfile;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceCableProfileRepository extends JpaRepository<DeviceCableProfile, UUID> {

    List<DeviceCableProfile> findAllByDeviceType_Id(UUID deviceTypeId);

    boolean existsByDeviceType_IdAndCableType_IdAndEndpointNameIgnoreCase(UUID deviceTypeId,
                                                                          UUID cableTypeId,
                                                                          String endpointName);
}
