package com.kapamejlbka.objectmanager.config;

import com.kapamejlbka.objectmanager.service.AppUserDetailsService;
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

    private final AppUserDetailsService userDetailsService;

    public SecurityConfig(AppUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // h2-console без CSRF
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))

                // Кому что разрешено
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/ping-open",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/h2-console/**"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().hasAnyRole("ADMIN", "ENGINEER", "VIEWER")
                )

                // Форма логина
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/admin", true)
                        .permitAll()
                )

                // Логаут
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )

                // Базовая авторизация выключена
                .httpBasic(AbstractHttpConfigurer::disable)

                // Наш UserDetailsService
                .userDetailsService(userDetailsService);

        // Разрешаем h2-console в iframe
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
