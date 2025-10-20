package com.kapamejlbka.objectmannage;

import com.kapamejlbka.objectmannage.model.ProjectCustomer;
import com.kapamejlbka.objectmannage.repository.ProjectCustomerRepository;
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
        SpringApplication.run(ObjectmannageApplication.class, args);
    }

    @Bean
    CommandLineRunner bootstrap(UserService userService, ProjectCustomerRepository customerRepository) {
        return args -> {
            userService.ensureDefaultUsers();
            if (customerRepository.count() == 0) {
                customerRepository.save(new ProjectCustomer("Заказчик А", "client-a@example.com", "+7 999 123-45-67"));
                customerRepository.save(new ProjectCustomer("Заказчик Б", "client-b@example.com", "+7 999 987-65-43"));
            }
        };
    }
}
