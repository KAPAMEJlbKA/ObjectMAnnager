package com.kapamejlbka.objectmanager.repository;

import com.kapamejlbka.objectmanager.model.ApplicationSetting;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationSettingRepository extends JpaRepository<ApplicationSetting, UUID> {
    Optional<ApplicationSetting> findBySettingKey(String settingKey);
}
