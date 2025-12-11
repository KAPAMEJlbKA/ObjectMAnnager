package com.kapamejlbka.objectmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Разрешаем h2-console без CSRF
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))

                .authorizeHttpRequests(auth -> auth
                        // Открытые маршруты (без авторизации)
                        .requestMatchers(
                                "/login",
                                "/auth/login",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/h2-console/**"
                        ).permitAll()

                        // Админка только для ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Всё остальное — только для авторизованных ролей
                        .anyRequest().hasAnyRole("ADMIN", "ENGINEER", "VIEWER")
                )

                // Форма логина
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/admin", true) // после логина идём в админку
                        .permitAll()
                )

                // Логаут
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )

                // Базовую HTTP-авторизацию не используем
                .httpBasic(AbstractHttpConfigurer::disable);

        // Разрешаем h2-console в iframe
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
