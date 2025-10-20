package com.kapamejlbka.objectmannage;

import com.kapamejlbka.objectmannage.config.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(excludeName = {
        "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration",
        "org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration",
        "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration",
        "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration",
        "org.springframework.boot.autoconfigure.mongo.MongoReactiveRepositoriesAutoConfiguration",
        "org.springframework.boot.autoconfigure.session.MongoSessionConfiguration"
})
@EnableConfigurationProperties(FileStorageProperties.class)
public class ObjectmannageApplication {

	public static void main(String[] args) {
		SpringApplication.run(ObjectmannageApplication.class, args);
	}

}
