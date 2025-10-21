package com.kapamejlbka.objectmannage.repository;

import com.kapamejlbka.objectmannage.model.ApplicationSetting;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationSettingRepository extends JpaRepository<ApplicationSetting, UUID> {
    Optional<ApplicationSetting> findBySettingKey(String settingKey);
}
