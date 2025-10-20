package com.kapamejlbka.objectmannage.service;

import com.kapamejlbka.objectmannage.model.DatabaseConnectionSettings;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class DatabaseSettingsService {

    private final Map<UUID, DatabaseConnectionSettings> settings = new ConcurrentHashMap<>();

    public Collection<DatabaseConnectionSettings> findAll() {
        return settings.values();
    }

    public DatabaseConnectionSettings save(String name, String host, int port, String databaseName, String username) {
        UUID id = UUID.randomUUID();
        DatabaseConnectionSettings config = new DatabaseConnectionSettings(id, name, host, port, databaseName, username);
        settings.put(id, config);
        return config;
    }
}
