package com.kapamejlbka.objectmanager.config;

import com.kapamejlbka.objectmanager.model.CableFunction;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CableTypeSchemaUpdater {

    private static final Logger log = LoggerFactory.getLogger(CableTypeSchemaUpdater.class);

    private final JdbcTemplate jdbcTemplate;

    public CableTypeSchemaUpdater(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureFunctionColumn() {
        try {
            Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_NAME) = ?",
                Integer.class,
                "CABLE_TYPES"
            );

            if (tableCount != null && tableCount == 0) {
                return;
            }

            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE UPPER(TABLE_NAME) = ? AND UPPER(COLUMN_NAME) = ?",
                Integer.class,
                "CABLE_TYPES",
                "FUNCTION"
            );

            if (count != null && count == 0) {
                log.info("Adding missing cable_types.function column");
                jdbcTemplate.execute("ALTER TABLE cable_types ADD COLUMN function VARCHAR(255) DEFAULT 'SIGNAL' NOT NULL");
                jdbcTemplate.update("UPDATE cable_types SET function = ? WHERE function IS NULL", CableFunction.SIGNAL.name());
            }
        } catch (DataAccessException ex) {
            log.warn("Failed to verify cable_types.function column", ex);
        }
    }
}
