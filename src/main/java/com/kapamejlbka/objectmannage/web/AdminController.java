package com.kapamejlbka.objectmannage.web;

import com.kapamejlbka.objectmannage.model.ManagedObject;
import com.kapamejlbka.objectmannage.model.UserAccount;
import com.kapamejlbka.objectmannage.repository.ProjectCustomerRepository;
import com.kapamejlbka.objectmannage.service.DatabaseSettingsService;
import com.kapamejlbka.objectmannage.service.ManagedObjectService;
import com.kapamejlbka.objectmannage.service.UserService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final DatabaseSettingsService databaseSettingsService;
    private final ManagedObjectService managedObjectService;
    private final ProjectCustomerRepository customerRepository;
    private final UserService userService;

    public AdminController(DatabaseSettingsService databaseSettingsService,
                           ManagedObjectService managedObjectService,
                           ProjectCustomerRepository customerRepository,
                           UserService userService) {
        this.databaseSettingsService = databaseSettingsService;
        this.managedObjectService = managedObjectService;
        this.customerRepository = customerRepository;
        this.userService = userService;
    }

    @GetMapping("/admin")
    public String adminPage(Model model) {
        model.addAttribute("connections", databaseSettingsService.findAll());
        model.addAttribute("dbForm", new DatabaseSettingsForm());
        model.addAttribute("h2Form", new H2Form());
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("pendingObjects", managedObjectService.listPendingDeletion());
        model.addAttribute("users", userService.findAll());
        model.addAttribute("userForm", new UserForm());
        return "admin/dashboard";
    }

    @PostMapping("/admin/database")
    public String saveDatabaseSettings(@ModelAttribute("dbForm") DatabaseSettingsForm form) {
        databaseSettingsService.configurePostgres(
                form.getName(),
                form.getHost(),
                form.getPort(),
                form.getDatabaseName(),
                form.getUsername(),
                form.getPassword()
        );
        return "redirect:/admin";
    }

    @PostMapping("/admin/database/h2")
    public String createH2(@ModelAttribute("h2Form") H2Form form) {
        databaseSettingsService.createLocalH2Store(form.getName());
        return "redirect:/admin";
    }

    @PostMapping("/admin/objects/{id}/delete")
    public String deleteObject(@PathVariable UUID id, Principal principal) {
        UserAccount admin = userService.findByUsername(principal.getName());
        managedObjectService.deletePermanently(id, admin);
        return "redirect:/admin";
    }

    @PostMapping("/admin/objects/{id}/transfer")
    public String transferObject(@PathVariable UUID id, @ModelAttribute("transferForm") TransferForm form, Principal principal) {
        UserAccount admin = userService.findByUsername(principal.getName());
        ManagedObject current = managedObjectService.getById(id);
        String name = form.getName() == null || form.getName().isBlank() ? current.getName() : form.getName();
        String description = form.getDescription() == null || form.getDescription().isBlank() ? current.getDescription() : form.getDescription();
        String primaryData = form.getPrimaryData() == null || form.getPrimaryData().isBlank() ? current.getPrimaryData() : form.getPrimaryData();
        managedObjectService.update(id, name, description, primaryData, form.getCustomerId(), admin);
        return "redirect:/admin";
    }

    @PostMapping("/admin/users")
    public String createUser(@ModelAttribute("userForm") UserForm form) {
        userService.createUser(form.getUsername(), form.getPassword(), form.isAdmin());
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
        private String password;

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

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class H2Form {
        @NotBlank
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class TransferForm {
        @NotNull
        private UUID customerId;
        private String name;
        private String description;
        private String primaryData;

        public UUID getCustomerId() {
            return customerId;
        }

        public void setCustomerId(UUID customerId) {
            this.customerId = customerId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getPrimaryData() {
            return primaryData;
        }

        public void setPrimaryData(String primaryData) {
            this.primaryData = primaryData;
        }
    }

    public static class UserForm {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
        private boolean admin;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isAdmin() {
            return admin;
        }

        public void setAdmin(boolean admin) {
            this.admin = admin;
        }
    }
}
