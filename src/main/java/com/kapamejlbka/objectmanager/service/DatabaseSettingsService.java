package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.model.DatabaseConnectionSettings;
import com.kapamejlbka.objectmanager.model.DatabaseType;
import com.kapamejlbka.objectmanager.repository.DatabaseConnectionSettingsRepository;
import jakarta.transaction.Transactional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.stereotype.Service;

@Service
public class DatabaseSettingsService {

    private final DatabaseConnectionSettingsRepository repository;
    private final DataSource dataSource;

    public DatabaseSettingsService(DatabaseConnectionSettingsRepository repository, DataSource dataSource) {
        this.repository = repository;
        this.dataSource = dataSource;
    }

    public void ensureConnectionSchema() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE database_connections ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT FALSE");
            statement.executeUpdate("UPDATE database_connections SET active = FALSE WHERE active IS NULL");
        } catch (SQLException ex) {
            if (!isMissingTableException(ex)) {
                throw new IllegalStateException("Не удалось обновить таблицу подключений", ex);
            }
        }
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
        Optional<DatabaseConnectionSettings> existing = repository.findByDatabaseTypeAndName(DatabaseType.H2, name);
        DatabaseConnectionSettings settings = existing.orElseGet(DatabaseConnectionSettings::new);
        settings.setName(name);
        settings.setDatabaseType(DatabaseType.H2);

        Path dataDirectory = Path.of("./data").toAbsolutePath().normalize();
        try {
            Files.createDirectories(dataDirectory);
        } catch (Exception ex) {
            throw new IllegalStateException("Не удалось создать каталог для H2 базы", ex);
        }

        Path dbFile = dataDirectory.resolve(name + ".mv.db");
        Path legacyFile = dataDirectory.resolve(name + ".h2.db");
        boolean existedBefore = Files.exists(dbFile) || Files.exists(legacyFile);

        String url = "jdbc:h2:file:" + dataDirectory.resolve(name).toString() + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
        settings.setDatabaseName(url);
        settings.setUsername("sa");
        settings.setPassword("");
        refreshH2Settings(settings, url, dataDirectory.resolve(name), existedBefore, true);
        return repository.save(settings);
    }

    @Transactional
    public void refreshLocalStores() {
        repository.findAll().stream()
                .filter(settings -> settings.getDatabaseType() == DatabaseType.H2)
                .forEach(settings -> {
                    String url = settings.getDatabaseName();
                    if (url == null || !url.startsWith("jdbc:h2:file:")) {
                        return;
                    }
                    Path databasePath = extractDatabasePath(url);
                    boolean existedBefore = databasePath != null
                            && (Files.exists(withExtension(databasePath, ".mv.db"))
                            || Files.exists(withExtension(databasePath, ".h2.db")));
                    refreshH2Settings(settings, url, databasePath, existedBefore, false);
                    repository.save(settings);
                });
    }

    @Transactional
    public boolean activateConnection(UUID id) {
        DatabaseConnectionSettings target = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Подключение не найдено"));

        revalidateConnection(target);

        if (!target.isInitialized()) {
            target.setActive(false);
            return false;
        }

        List<DatabaseConnectionSettings> allConnections = repository.findAll();
        for (DatabaseConnectionSettings connection : allConnections) {
            boolean shouldActivate = connection.getId().equals(id);
            connection.setActive(shouldActivate);
        }
        return true;
    }

    @Transactional
    public void deactivateConnection(UUID id) {
        repository.findById(id).ifPresent(connection -> connection.setActive(false));
    }

    private void refreshH2Settings(DatabaseConnectionSettings settings, String url, Path databasePath,
                                   boolean existedBefore, boolean allowCreate) {
        Path parentDirectory = databasePath != null ? databasePath.getParent() : null;
        if (parentDirectory != null) {
            try {
                Files.createDirectories(parentDirectory);
            } catch (Exception ex) {
                settings.setInitialized(false);
                settings.setStatusMessage("Не удалось подготовить каталог: " + ex.getMessage());
                settings.setLastVerifiedAt(LocalDateTime.now());
                return;
            }
        }

        if (!allowCreate && !existedBefore) {
            settings.setInitialized(false);
            settings.setStatusMessage("Файл H2 базы не найден по пути "
                    + (databasePath != null ? databasePath : "(не указан)"));
            settings.setLastVerifiedAt(LocalDateTime.now());
            return;
        }

        try (Connection connection = DriverManager.getConnection(url, settings.getUsername(), settings.getPassword())) {
            initializeSchema(connection);
            settings.setInitialized(true);
            settings.setStatusMessage(existedBefore
                    ? "Подключено к существующей H2 базе"
                    : "Файловая H2 база создана");
        } catch (SQLException ex) {
            settings.setInitialized(false);
            settings.setStatusMessage("Ошибка подключения к H2: " + ex.getMessage());
        }
        settings.setLastVerifiedAt(LocalDateTime.now());
    }

    private void revalidateConnection(DatabaseConnectionSettings settings) {
        if (settings.getDatabaseType() == DatabaseType.H2) {
            String url = settings.getDatabaseName();
            if (url == null || url.isBlank()) {
                settings.setInitialized(false);
                settings.setStatusMessage("URL H2 базы данных не задан");
                settings.setLastVerifiedAt(LocalDateTime.now());
                return;
            }
            Path databasePath = extractDatabasePath(url);
            boolean existedBefore = databasePath != null
                    && (Files.exists(withExtension(databasePath, ".mv.db"))
                    || Files.exists(withExtension(databasePath, ".h2.db")));
            refreshH2Settings(settings, url, databasePath, existedBefore, false);
        } else {
            verifyAndInitialize(settings);
        }
    }

    private Path extractDatabasePath(String url) {
        String withoutPrefix = url.substring("jdbc:h2:file:".length());
        int separatorIndex = withoutPrefix.indexOf(';');
        String pathPart = separatorIndex >= 0 ? withoutPrefix.substring(0, separatorIndex) : withoutPrefix;
        if (pathPart.isBlank()) {
            return null;
        }
        return Path.of(pathPart).toAbsolutePath().normalize();
    }

    private Path withExtension(Path databasePath, String extension) {
        return Path.of(databasePath.toString() + extension);
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
                        content_type VARCHAR(255),
                        object_id UUID REFERENCES managed_objects(id)
                    )
                    """);
            statement.executeUpdate("""
                    ALTER TABLE stored_files ADD COLUMN IF NOT EXISTS content_type VARCHAR(255)
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
            ensureLongTextColumn(statement, "object_changes", "old_value");
            ensureLongTextColumn(statement, "object_changes", "new_value");
        }
    }

    private void ensureLongTextColumn(Statement statement, String table, String column) {
        tryExecute(statement, "ALTER TABLE " + table + " ALTER COLUMN " + column + " SET DATA TYPE TEXT");
        tryExecute(statement, "ALTER TABLE " + table + " ALTER COLUMN " + column + " TYPE TEXT");
    }

    private void tryExecute(Statement statement, String sql) {
        try {
            statement.executeUpdate(sql);
        } catch (SQLException ignored) {
            // Если команда не поддерживается БД или тип уже обновлён, просто пропускаем
        }
    }

    private boolean isMissingTableException(SQLException ex) {
        String sqlState = ex.getSQLState();
        if ("42S02".equals(sqlState) || "42P01".equals(sqlState) || ex.getErrorCode() == 42102) {
            return true;
        }
        String message = ex.getMessage();
        return message != null && message.contains("database_connections") && message.contains("not found");
    }
}
