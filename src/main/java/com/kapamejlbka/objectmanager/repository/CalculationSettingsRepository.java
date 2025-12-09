package com.kapamejlbka.objectmanager.repository;

import com.kapamejlbka.objectmanager.domain.settings.CalculationSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalculationSettingsRepository extends JpaRepository<CalculationSettings, Long> {
}
