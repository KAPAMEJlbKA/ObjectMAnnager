package com.kapamejlbka.objectmanager.web;

import com.kapamejlbka.objectmanager.domain.material.Material;
import com.kapamejlbka.objectmanager.domain.material.MaterialCategory;
import com.kapamejlbka.objectmanager.domain.material.MaterialNorm;
import com.kapamejlbka.objectmanager.domain.material.MaterialNormContext;
import com.kapamejlbka.objectmanager.domain.material.dto.MaterialForm;
import com.kapamejlbka.objectmanager.domain.material.dto.MaterialNormForm;
import com.kapamejlbka.objectmanager.domain.customer.ProjectCustomer;
import com.kapamejlbka.objectmanager.domain.settings.dto.CalculationSettingsDto;
import com.kapamejlbka.objectmanager.domain.user.AppRole;
import com.kapamejlbka.objectmanager.domain.user.AppUser;
import com.kapamejlbka.objectmanager.domain.user.dto.UserCreateRequest;
import com.kapamejlbka.objectmanager.domain.user.dto.UserUpdateRequest;
import com.kapamejlbka.objectmanager.model.DatabaseConnectionSettings;
import com.kapamejlbka.objectmanager.service.CustomerAccessService;
import com.kapamejlbka.objectmanager.service.DatabaseSettingsService;
import com.kapamejlbka.objectmanager.service.MaterialNormService;
import com.kapamejlbka.objectmanager.service.MaterialService;
import com.kapamejlbka.objectmanager.service.SettingsService;
import com.kapamejlbka.objectmanager.service.UserService;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final SettingsService settingsService;
    private final MaterialService materialService;
    private final MaterialNormService materialNormService;
    private final DatabaseSettingsService databaseSettingsService;
    private final CustomerAccessService customerAccessService;

    public AdminController(
            UserService userService,
            SettingsService settingsService,
            MaterialService materialService,
            MaterialNormService materialNormService,
            DatabaseSettingsService databaseSettingsService,
            CustomerAccessService customerAccessService) {
        this.userService = userService;
        this.settingsService = settingsService;
        this.materialService = materialService;
        this.materialNormService = materialNormService;
        this.databaseSettingsService = databaseSettingsService;
        this.customerAccessService = customerAccessService;
    }

    @GetMapping("/admin")
    public String dashboard(
            @RequestParam(value = "tab", defaultValue = "users") String tab,
            @RequestParam(value = "editUserId", required = false) Long editUserId,
            @RequestParam(value = "editMaterialId", required = false) Long editMaterialId,
            @RequestParam(value = "editNormId", required = false) Long editNormId,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "customer", required = false) UUID selectedCustomer,
            Model model) {
        populateDashboardModel(model, tab, editUserId, editMaterialId, editNormId, search, category, selectedCustomer);
        return "admin/dashboard";
    }

    @PostMapping("/admin/users")
    public String createUser(@ModelAttribute("userForm") UserCreateRequest form, RedirectAttributes redirectAttributes) {
        try {
            normalizeUserForm(form);
            userService.createUser(form);
            redirectAttributes.addFlashAttribute("flashSuccess", "Пользователь создан");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        redirectAttributes.addAttribute("tab", "users");
        return "redirect:/admin";
    }

    @PostMapping("/admin/users/{id}")
    public String updateUser(
            @PathVariable("id") Long id,
            @ModelAttribute("editUserForm") UserUpdateRequest form,
            RedirectAttributes redirectAttributes) {
        try {
            normalizeUserUpdateForm(form);
            userService.updateUser(id, form);
            redirectAttributes.addFlashAttribute("flashSuccess", "Пользователь обновлён");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
            redirectAttributes.addAttribute("editUserId", id);
        }
        redirectAttributes.addAttribute("tab", "users");
        return "redirect:/admin";
    }

    @PostMapping("/admin/users/{id}/status")
    public String updateUserStatus(
            @PathVariable("id") Long id,
            @RequestParam("enabled") boolean enabled,
            RedirectAttributes redirectAttributes) {
        try {
            userService.updateStatus(id, enabled);
            redirectAttributes.addFlashAttribute("flashSuccess", enabled ? "Пользователь разблокирован" : "Пользователь заблокирован");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
            redirectAttributes.addAttribute("editUserId", id);
        }
        redirectAttributes.addAttribute("tab", "users");
        return "redirect:/admin";
    }

    @PostMapping("/admin/settings")
    public String updateSettings(
            @ModelAttribute("settings") CalculationSettingsDto settings,
            RedirectAttributes redirectAttributes) {
        try {
            settingsService.updateSettings(settings);
            redirectAttributes.addFlashAttribute("flashSuccess", "Настройки сохранены");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        redirectAttributes.addAttribute("tab", "settings");
        return "redirect:/admin";
    }

    @PostMapping("/admin/materials")
    public String createMaterial(@ModelAttribute("materialForm") MaterialForm form, RedirectAttributes redirectAttributes) {
        try {
            materialService.create(form);
            redirectAttributes.addFlashAttribute("flashSuccess", "Материал добавлен");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        redirectAttributes.addAttribute("tab", "materials");
        return "redirect:/admin";
    }

    @PostMapping("/admin/materials/{id}")
    public String updateMaterial(
            @PathVariable("id") Long id,
            @ModelAttribute("editMaterialForm") MaterialForm form,
            RedirectAttributes redirectAttributes) {
        try {
            materialService.update(id, form);
            redirectAttributes.addFlashAttribute("flashSuccess", "Материал обновлён");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
            redirectAttributes.addAttribute("editMaterialId", id);
        }
        redirectAttributes.addAttribute("tab", "materials");
        return "redirect:/admin";
    }

    @PostMapping("/admin/customers/{id}/access")
    public String grantCustomerAccess(
            @PathVariable("id") UUID customerId,
            @RequestParam(value = "customerId", required = false) UUID customerIdFromForm,
            @RequestParam("userId") Long userId,
            RedirectAttributes redirectAttributes) {
        try {
            UUID targetCustomer = customerIdFromForm == null ? customerId : customerIdFromForm;
            customerAccessService.grantAccess(targetCustomer, userId);
            redirectAttributes.addFlashAttribute("flashSuccess", "Доступ выдан");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        redirectAttributes.addAttribute("tab", "access");
        redirectAttributes.addAttribute("customer", customerIdFromForm == null ? customerId : customerIdFromForm);
        return "redirect:/admin";
    }

    @PostMapping("/admin/customers/{id}/access/revoke")
    public String revokeCustomerAccess(
            @PathVariable("id") UUID customerId,
            @RequestParam(value = "customerId", required = false) UUID customerIdFromForm,
            @RequestParam("userId") Long userId,
            RedirectAttributes redirectAttributes) {
        try {
            UUID targetCustomer = customerIdFromForm == null ? customerId : customerIdFromForm;
            customerAccessService.revokeAccess(targetCustomer, userId);
            redirectAttributes.addFlashAttribute("flashSuccess", "Доступ удалён");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        redirectAttributes.addAttribute("tab", "access");
        redirectAttributes.addAttribute("customer", customerIdFromForm == null ? customerId : customerIdFromForm);
        return "redirect:/admin";
    }

    @PostMapping("/admin/materials/{id}/delete")
    public String deleteMaterial(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            materialService.delete(id);
            redirectAttributes.addFlashAttribute("flashSuccess", "Материал удалён");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        redirectAttributes.addAttribute("tab", "materials");
        return "redirect:/admin";
    }

    @PostMapping("/admin/norms")
    public String createNorm(@ModelAttribute("normForm") MaterialNormForm form, RedirectAttributes redirectAttributes) {
        try {
            materialNormService.create(form);
            redirectAttributes.addFlashAttribute("flashSuccess", "Норма создана");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        redirectAttributes.addAttribute("tab", "norms");
        return "redirect:/admin";
    }

    @PostMapping("/admin/norms/{id}")
    public String updateNorm(
            @PathVariable("id") Long id,
            @ModelAttribute("editNormForm") MaterialNormForm form,
            RedirectAttributes redirectAttributes) {
        try {
            materialNormService.update(id, form);
            redirectAttributes.addFlashAttribute("flashSuccess", "Норма обновлена");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
            redirectAttributes.addAttribute("editNormId", id);
        }
        redirectAttributes.addAttribute("tab", "norms");
        return "redirect:/admin";
    }

    @PostMapping("/admin/norms/{id}/delete")
    public String deleteNorm(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            materialNormService.delete(id);
            redirectAttributes.addFlashAttribute("flashSuccess", "Норма удалена");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        redirectAttributes.addAttribute("tab", "norms");
        return "redirect:/admin";
    }

    @PostMapping("/admin/databases/postgres")
    public String configurePostgres(
            @RequestParam("name") String name,
            @RequestParam("host") String host,
            @RequestParam("port") int port,
            @RequestParam("databaseName") String databaseName,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            RedirectAttributes redirectAttributes) {
        try {
            DatabaseConnectionSettings settings = databaseSettingsService.configurePostgres(
                    name,
                    host,
                    port,
                    databaseName,
                    username,
                    password);
            redirectAttributes.addFlashAttribute("flashSuccess", settings.getStatusMessage());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        redirectAttributes.addAttribute("tab", "databases");
        return "redirect:/admin";
    }

    @PostMapping("/admin/databases/h2")
    public String createLocalH2Store(
            @RequestParam("name") String name,
            RedirectAttributes redirectAttributes) {
        try {
            DatabaseConnectionSettings settings = databaseSettingsService.createLocalH2Store(name);
            redirectAttributes.addFlashAttribute("flashSuccess", settings.getStatusMessage());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        redirectAttributes.addAttribute("tab", "databases");
        return "redirect:/admin";
    }

    @PostMapping("/admin/databases/{id}/activate")
    public String activateConnection(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        try {
            boolean activated = databaseSettingsService.activateConnection(id);
            String statusMessage = findStatusMessage(id);
            if (activated) {
                redirectAttributes.addFlashAttribute(
                        "flashSuccess",
                        statusMessage == null || statusMessage.isBlank()
                                ? "Подключение активировано"
                                : statusMessage);
            } else {
                redirectAttributes.addFlashAttribute(
                        "flashError",
                        statusMessage == null || statusMessage.isBlank()
                                ? "Не удалось активировать подключение"
                                : statusMessage);
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        redirectAttributes.addAttribute("tab", "databases");
        return "redirect:/admin";
    }

    @PostMapping("/admin/databases/{id}/deactivate")
    public String deactivateConnection(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        databaseSettingsService.deactivateConnection(id);
        redirectAttributes.addFlashAttribute("flashSuccess", "Подключение деактивировано");
        redirectAttributes.addAttribute("tab", "databases");
        return "redirect:/admin";
    }

    private void populateDashboardModel(
            Model model,
            String tab,
            Long editUserId,
            Long editMaterialId,
            Long editNormId,
            String search,
            String category,
            UUID selectedCustomer) {
        String activeTab = (tab == null || tab.isBlank()) ? "users" : tab;
        model.addAttribute("activeTab", activeTab);

        List<AppUser> users = userService.findAll().stream()
                .sorted(Comparator.comparing(AppUser::getUsername, String.CASE_INSENSITIVE_ORDER))
                .toList();
        List<AppRole> roles = userService.findAllRoles().stream()
                .sorted(Comparator.comparing(AppRole::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        UserCreateRequest userForm = new UserCreateRequest();
        userForm.setEnabled(true);
        model.addAttribute("users", users);
        model.addAttribute("roles", roles);
        model.addAttribute("userForm", userForm);
        if (editUserId != null) {
            AppUser user = userService.getById(editUserId);
            UserUpdateRequest editForm = new UserUpdateRequest();
            editForm.setId(user.getId());
            editForm.setUsername(user.getUsername());
            editForm.setEmail(user.getEmail());
            editForm.setFullName(user.getFullName());
            editForm.setEnabled(user.isEnabled());
            editForm.setRoles(user.getRoles().stream().map(AppRole::getName).collect(java.util.stream.Collectors.toSet()));
            model.addAttribute("editUserForm", editForm);
        }

        CalculationSettingsDto settings = settingsService.getSettings();
        model.addAttribute("settings", settings);

        MaterialCategory selectedCategory = parseCategory(category);
        List<MaterialCategory> categories = materialService.listCategories();
        List<Material> materials = materialService.search(search, selectedCategory);
        MaterialForm materialForm = new MaterialForm();
        model.addAttribute("materials", materials);
        model.addAttribute("materialCategories", categories);
        model.addAttribute("materialForm", materialForm);
        model.addAttribute("materialQuery", search == null ? "" : search);
        model.addAttribute("materialCategory", selectedCategory);
        if (editMaterialId != null) {
            Material material = materialService.getById(editMaterialId);
            MaterialForm editForm = new MaterialForm();
            editForm.setId(material.getId());
            editForm.setCode(material.getCode());
            editForm.setName(material.getName());
            editForm.setCategory(material.getCategory());
            editForm.setUnit(material.getUnit());
            editForm.setNotes(material.getNotes());
            model.addAttribute("editMaterialForm", editForm);
        }

        List<MaterialNorm> norms = materialNormService.listAll();
        norms.sort(Comparator.comparing(
                norm -> norm.getContextType() == null ? "" : norm.getContextType().name(),
                String.CASE_INSENSITIVE_ORDER));
        MaterialNormForm normForm = new MaterialNormForm();
        model.addAttribute("norms", norms);
        model.addAttribute("normForm", normForm);
        model.addAttribute("normMaterials", materialService.listAll().stream()
                .sorted(Comparator.comparing(Material::getCode, String.CASE_INSENSITIVE_ORDER))
                .toList());
        model.addAttribute("normContexts", MaterialNormContext.orderedValues());
        model.addAttribute("normContextNames", MaterialNormContext.availableContexts());
        if (editNormId != null) {
            MaterialNorm norm = materialNormService.getById(editNormId);
            MaterialNormForm editForm = new MaterialNormForm();
            editForm.setId(norm.getId());
            editForm.setContextType(norm.getContextType());
            editForm.setMaterialId(norm.getMaterial() != null ? norm.getMaterial().getId() : null);
            editForm.setFormula(norm.getFormula());
            editForm.setDescription(norm.getDescription());
            model.addAttribute("editNormForm", editForm);
        }

        List<DatabaseConnectionSettings> connections = databaseSettingsService.findAll().stream()
                .sorted(Comparator.comparing(DatabaseConnectionSettings::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        model.addAttribute("connections", connections);

        List<ProjectCustomer> customers = customerAccessService.findAll().stream()
                .sorted(Comparator.comparing(ProjectCustomer::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        UUID effectiveCustomer = selectedCustomer;
        if (effectiveCustomer == null && !customers.isEmpty()) {
            effectiveCustomer = customers.get(0).getId();
        }
        model.addAttribute("projectCustomers", customers);
        model.addAttribute("selectedCustomer", effectiveCustomer);
    }

    private void normalizeUserForm(UserCreateRequest form) {
        if (StringUtils.hasText(form.getUsername())) {
            form.setUsername(form.getUsername().trim());
        }
        if (StringUtils.hasText(form.getEmail())) {
            form.setEmail(form.getEmail().trim().toLowerCase(Locale.ROOT));
        }
    }

    private MaterialCategory parseCategory(String category) {
        if (!StringUtils.hasText(category)) {
            return null;
        }
        try {
            return MaterialCategory.valueOf(category.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private void normalizeUserUpdateForm(UserUpdateRequest form) {
        if (StringUtils.hasText(form.getEmail())) {
            form.setEmail(form.getEmail().trim().toLowerCase(Locale.ROOT));
        }
    }

    private String findStatusMessage(UUID id) {
        return databaseSettingsService.findAll().stream()
                .filter(connection -> id.equals(connection.getId()))
                .map(DatabaseConnectionSettings::getStatusMessage)
                .findFirst()
                .orElse(null);
    }
}
