package com.kapamejlbka.objectmanager;

import com.kapamejlbka.objectmanager.service.DatabaseSettingsService;
import com.kapamejlbka.objectmanager.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        scanBasePackages = {
                "com.kapamejlbka.objectmanager",
                "com.kapamejlbka.objectmannage"
        },
        excludeName = {
        "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration",
        "org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration",
        "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration",
        "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration",
        "org.springframework.boot.autoconfigure.mongo.MongoReactiveRepositoriesAutoConfiguration",
        "org.springframework.boot.autoconfigure.session.MongoSessionConfiguration"
})
@EnableJpaRepositories({"com.kapamejlbka.objectmanager.repository", "com.kapamejlbka.objectmannage.repository"})
@EnableConfigurationProperties(com.kapamejlbka.objectmanager.config.FileStorageProperties.class)
public class ObjectManagerApplication {

    public static void main(String[] args) {
        ensureLocalDatabaseDirectory();
        SpringApplication.run(ObjectManagerApplication.class, args);
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
