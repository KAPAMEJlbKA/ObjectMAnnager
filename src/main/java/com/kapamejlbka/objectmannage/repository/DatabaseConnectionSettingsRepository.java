package com.kapamejlbka.objectmannage.repository;

import com.kapamejlbka.objectmannage.model.DatabaseConnectionSettings;
import com.kapamejlbka.objectmannage.model.DatabaseType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatabaseConnectionSettingsRepository extends JpaRepository<DatabaseConnectionSettings, UUID> {
    Optional<DatabaseConnectionSettings> findByDatabaseTypeAndName(DatabaseType databaseType, String name);
}
