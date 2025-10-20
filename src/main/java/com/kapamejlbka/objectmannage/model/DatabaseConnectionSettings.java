package com.kapamejlbka.objectmannage.model;

import java.util.UUID;

public class DatabaseConnectionSettings {

    private final UUID id;
    private String name;
    private String host;
    private int port;
    private String databaseName;
    private String username;

    public DatabaseConnectionSettings(UUID id, String name, String host, int port, String databaseName, String username) {
        this.id = id;
        this.name = name;
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.username = username;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
