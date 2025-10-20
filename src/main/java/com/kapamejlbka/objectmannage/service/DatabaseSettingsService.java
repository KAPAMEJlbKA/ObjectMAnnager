package com.kapamejlbka.objectmannage.service;

import com.kapamejlbka.objectmannage.model.DatabaseConnectionSettings;
import com.kapamejlbka.objectmannage.model.DatabaseType;
import com.kapamejlbka.objectmannage.repository.DatabaseConnectionSettingsRepository;
import jakarta.transaction.Transactional;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Collection;
import org.springframework.stereotype.Service;

@Service
public class DatabaseSettingsService {

    private final DatabaseConnectionSettingsRepository repository;

    public DatabaseSettingsService(DatabaseConnectionSettingsRepository repository) {
        this.repository = repository;
    }

    public Collection<DatabaseConnectionSettings> findAll() {
        return repository.findAll();
    }

    @Transactional
    public DatabaseConnectionSettings configurePostgres(String name, String host, int port, String databaseName,
                                                        String username, String password) {
        DatabaseConnectionSettings settings = new DatabaseConnectionSettings();
        settings.setName(name);
        settings.setHost(host);
        settings.setPort(port);
        settings.setDatabaseName(databaseName);
        settings.setUsername(username);
        settings.setPassword(password);
        settings.setDatabaseType(DatabaseType.POSTGRESQL);
        verifyAndInitialize(settings);
        return repository.save(settings);
    }

    @Transactional
    public DatabaseConnectionSettings createLocalH2Store(String name) {
        DatabaseConnectionSettings settings = new DatabaseConnectionSettings();
        settings.setName(name);
        settings.setDatabaseType(DatabaseType.H2);
        String url = "jdbc:h2:file:./data/" + name + ";AUTO_SERVER=TRUE";
        settings.setDatabaseName(url);
        settings.setUsername("sa");
        settings.setPassword("");
        try (Connection connection = DriverManager.getConnection(url, settings.getUsername(), settings.getPassword())) {
            initializeSchema(connection);
            settings.setInitialized(true);
            settings.setStatusMessage("Файловая H2 база создана");
        } catch (SQLException ex) {
            settings.setInitialized(false);
            settings.setStatusMessage("Ошибка создания H2: " + ex.getMessage());
        }
        settings.setLastVerifiedAt(LocalDateTime.now());
        return repository.save(settings);
    }

    private void verifyAndInitialize(DatabaseConnectionSettings settings) {
        String url = "jdbc:postgresql://" + settings.getHost() + ":" + settings.getPort() + "/" + settings.getDatabaseName();
        try (Connection connection = DriverManager.getConnection(url, settings.getUsername(), settings.getPassword())) {
            initializeSchema(connection);
            settings.setInitialized(true);
            settings.setLastVerifiedAt(LocalDateTime.now());
            settings.setStatusMessage("Подключение успешно. Таблицы созданы");
        } catch (SQLException ex) {
            settings.setInitialized(false);
            settings.setLastVerifiedAt(LocalDateTime.now());
            settings.setStatusMessage("Ошибка подключения: " + ex.getMessage());
        }
    }

    private void initializeSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS project_customers (
                        id UUID PRIMARY KEY,
                        name VARCHAR(255) UNIQUE NOT NULL,
                        contact_email VARCHAR(255),
                        contact_phone VARCHAR(255),
                        created_at TIMESTAMP
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS managed_objects (
                        id UUID PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        description TEXT,
                        primary_data TEXT,
                        customer_id UUID NOT NULL,
                        created_at TIMESTAMP,
                        updated_at TIMESTAMP,
                        deletion_requested BOOLEAN DEFAULT FALSE,
                        deletion_requested_at TIMESTAMP,
                        deletion_requested_by UUID,
                        CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES project_customers(id)
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS stored_files (
                        id UUID PRIMARY KEY,
                        original_filename VARCHAR(512),
                        stored_filename VARCHAR(512),
                        size BIGINT,
                        uploaded_at TIMESTAMP,
                        object_id UUID REFERENCES managed_objects(id)
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS user_accounts (
                        id UUID PRIMARY KEY,
                        username VARCHAR(128) UNIQUE NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        enabled BOOLEAN DEFAULT TRUE,
                        created_at TIMESTAMP
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS user_roles (
                        user_id UUID REFERENCES user_accounts(id),
                        role VARCHAR(64)
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS object_changes (
                        id UUID PRIMARY KEY,
                        object_id UUID REFERENCES managed_objects(id),
                        user_id UUID REFERENCES user_accounts(id),
                        changed_at TIMESTAMP,
                        change_type VARCHAR(64),
                        field_name VARCHAR(255),
                        old_value TEXT,
                        new_value TEXT,
                        summary TEXT
                    )
                    """);
        }
    }
}
