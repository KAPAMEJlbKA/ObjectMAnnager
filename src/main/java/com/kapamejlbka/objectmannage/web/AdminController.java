package com.kapamejlbka.objectmannage.web;

import com.kapamejlbka.objectmannage.service.DatabaseSettingsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@Validated
public class AdminController {

    private final DatabaseSettingsService databaseSettingsService;

    public AdminController(DatabaseSettingsService databaseSettingsService) {
        this.databaseSettingsService = databaseSettingsService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String adminPage(Model model) {
        model.addAttribute("connections", databaseSettingsService.findAll());
        model.addAttribute("form", new DatabaseSettingsForm());
        return "admin/dashboard";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/database")
    public String saveDatabaseSettings(@ModelAttribute("form") DatabaseSettingsForm form) {
        databaseSettingsService.save(form.getName(), form.getHost(), form.getPort(), form.getDatabaseName(), form.getUsername());
        return "redirect:/admin";
    }

    public static class DatabaseSettingsForm {
        @NotBlank
        private String name;
        @NotBlank
        private String host;
        @Min(1)
        @Max(65535)
        private int port = 5432;
        @NotBlank
        private String databaseName;
        @NotBlank
        private String username;

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
}
