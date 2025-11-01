package com.kapamejlbka.objectmanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kapamejlbka.objectmanager.model.PrimaryDataSnapshot;
import com.kapamejlbka.objectmanager.model.primarydata.PrimaryDataV2;
import java.io.IOException;
import java.util.Objects;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class PrimaryDataParser {

    private final ObjectMapper objectMapper;
    private final PrimaryDataV2ToSnapshotConverter v2Converter;

    public PrimaryDataParser(ObjectProvider<ObjectMapper> objectMapperProvider,
                             PrimaryDataV2ToSnapshotConverter v2Converter) {
        this.objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
        this.v2Converter = v2Converter;
    }

    public SchemaVersion detectVersion(String json) {
        if (!hasText(json)) {
            return SchemaVersion.V1;
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode versionNode = root.get("schemaVersion");
            if (versionNode != null && versionNode.isInt() && versionNode.intValue() >= 2) {
                return SchemaVersion.V2;
            }
        } catch (IOException ignored) {
            // fall back to heuristics below
        }
        if (json.contains("\"schemaVersion\"")) {
            return SchemaVersion.V2;
        }
        return SchemaVersion.V1;
    }

    public ParsingResult parse(String json, SchemaVersion version) {
        if (!hasText(json)) {
            return ParsingResult.empty();
        }
        try {
            if (version == SchemaVersion.V2) {
                PrimaryDataV2 payload = objectMapper.readValue(json, PrimaryDataV2.class);
                PrimaryDataSnapshot snapshot = v2Converter.convert(payload);
                return ParsingResult.success(snapshot);
            }
            PrimaryDataSnapshot snapshot = objectMapper.readValue(json, PrimaryDataSnapshot.class);
            return ParsingResult.success(snapshot);
        } catch (JsonProcessingException ex) {
            return ParsingResult.error(ex.getOriginalMessage());
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public enum SchemaVersion {
        V1,
        V2
    }

    public record ParsingResult(PrimaryDataSnapshot snapshot, String errorMessage) {

        static ParsingResult success(PrimaryDataSnapshot snapshot) {
            return new ParsingResult(snapshot, null);
        }

        static ParsingResult error(String message) {
            return new ParsingResult(null, message);
        }

        static ParsingResult empty() {
            return new ParsingResult(null, null);
        }

        public boolean hasError() {
            return errorMessage != null;
        }

        public boolean hasSnapshot() {
            return snapshot != null;
        }
    }
}
