package com.kapamejlbka.objectmannage;

import com.kapamejlbka.objectmannage.service.DatabaseSettingsService;
import com.kapamejlbka.objectmannage.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(excludeName = {
        "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration",
        "org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration",
        "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration",
        "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration",
        "org.springframework.boot.autoconfigure.mongo.MongoReactiveRepositoriesAutoConfiguration",
        "org.springframework.boot.autoconfigure.session.MongoSessionConfiguration"
})
@EnableConfigurationProperties(com.kapamejlbka.objectmannage.config.FileStorageProperties.class)
public class ObjectmannageApplication {

    public static void main(String[] args) {
        ensureLocalDatabaseDirectory();
        SpringApplication.run(ObjectmannageApplication.class, args);
    }

    @Bean
    CommandLineRunner bootstrap(UserService userService, DatabaseSettingsService databaseSettingsService) {
        return args -> {
            databaseSettingsService.ensureConnectionSchema();
            databaseSettingsService.refreshLocalStores();
            userService.ensureDefaultUsers();
        };
    }

    private static void ensureLocalDatabaseDirectory() {
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Path.of("./data").toAbsolutePath().normalize());
        } catch (java.io.IOException ex) {
            throw new IllegalStateException("Не удалось создать каталог для локальной базы данных", ex);
        }
    }
}
