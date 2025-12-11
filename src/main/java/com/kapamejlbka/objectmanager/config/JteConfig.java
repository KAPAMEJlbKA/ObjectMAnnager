package com.kapamejlbka.objectmanager.config;

import gg.jte.TemplateEngine;
import gg.jte.ContentType;
import gg.jte.spring.boot3.JteViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;

@Configuration
public class JteConfig {

    /**
     * Создаём TemplateEngine, который будет читать шаблоны из src/main/jte.
     * При сборке Gradle jte-шаблоны компилируются, и движок их подхватывает.
     */
    @Bean
    public TemplateEngine jteTemplateEngine() {
        // DEV-вариант: использовать файловую систему src/main/jte
        return TemplateEngine.createPrecompiled(
                ContentType.Html
        );
    }

    /**
     * Регистрируем JteViewResolver, который будет искать шаблоны по имени вида "auth/login"
     * как файлы src/main/jte/templates/auth/login.jte
     */
    @Bean
    public ViewResolver jteViewResolver(TemplateEngine templateEngine) {
        JteViewResolver resolver = new JteViewResolver();
        resolver.setTemplateEngine(templateEngine);
        resolver.setPrefix("templates/");
        resolver.setSuffix(".jte");
        resolver.setOrder(0); // приоритет выше дефолтных резолверов
        return resolver;
    }
}
