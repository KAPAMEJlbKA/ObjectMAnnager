package com.kapamejlbka.objectmannage.repository;

import com.kapamejlbka.objectmannage.model.DatabaseConnectionSettings;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatabaseConnectionSettingsRepository extends JpaRepository<DatabaseConnectionSettings, UUID> {
}
