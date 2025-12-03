package com.kapamejlbka.objectmanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
        "com.kapamejlbka.objectmanager.domain",
        "com.kapamejlbka.objectmanager.repository",
        "com.kapamejlbka.objectmanager.legacy.objectmannage.repository"
})
public class DomainJpaConfiguration {
}
