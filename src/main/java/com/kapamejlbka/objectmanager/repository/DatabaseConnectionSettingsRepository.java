package com.kapamejlbka.objectmanager.repository;

import com.kapamejlbka.objectmanager.model.DatabaseConnectionSettings;
import com.kapamejlbka.objectmanager.model.DatabaseType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatabaseConnectionSettingsRepository extends JpaRepository<DatabaseConnectionSettings, UUID> {
    Optional<DatabaseConnectionSettings> findByDatabaseTypeAndName(DatabaseType databaseType, String name);
}
