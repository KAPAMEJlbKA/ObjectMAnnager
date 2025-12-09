package com.kapamejlbka.objectmanager.legacy.web;

import com.kapamejlbka.objectmanager.domain.device.CableFunction;
import com.kapamejlbka.objectmanager.domain.device.CableType;
import com.kapamejlbka.objectmanager.domain.device.DeviceType;
import com.kapamejlbka.objectmanager.domain.customer.ManagedObject;
import com.kapamejlbka.objectmanager.domain.topology.MapProvider;
import com.kapamejlbka.objectmanager.domain.device.MountingElement;
import com.kapamejlbka.objectmanager.domain.user.AppUser;
import com.kapamejlbka.objectmanager.domain.device.InstallationMaterial;
import com.kapamejlbka.objectmanager.domain.customer.repository.ProjectCustomerRepository;
import com.kapamejlbka.objectmanager.domain.device.repository.CableTypeRepository;
import com.kapamejlbka.objectmanager.domain.device.repository.DeviceTypeRepository;
import com.kapamejlbka.objectmanager.domain.device.repository.MountingElementRepository;
import com.kapamejlbka.objectmanager.domain.device.repository.InstallationMaterialRepository;
import com.kapamejlbka.objectmanager.service.DatabaseSettingsService;
import com.kapamejlbka.objectmanager.service.ManagedObjectService;
import com.kapamejlbka.objectmanager.service.UserService;
import com.kapamejlbka.objectmanager.service.ApplicationSettingsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.io.IOException;
import java.security.Principal;
import java.util.Base64;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

@Controller("legacyAdminController")
@Validated
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/legacy")
public class AdminController {

    private final DatabaseSettingsService databaseSettingsService;
    private final ManagedObjectService managedObjectService;
    private final ProjectCustomerRepository customerRepository;
    private final DeviceTypeRepository deviceTypeRepository;
    private final CableTypeRepository cableTypeRepository;
    private final MountingElementRepository mountingElementRepository;
    private final InstallationMaterialRepository installationMaterialRepository;
    private final ApplicationSettingsService applicationSettingsService;
    private final UserService userService;

    public AdminController(DatabaseSettingsService databaseSettingsService,
                           ManagedObjectService managedObjectService,
                           ProjectCustomerRepository customerRepository,
                           DeviceTypeRepository deviceTypeRepository,
                           CableTypeRepository cableTypeRepository,
                           MountingElementRepository mountingElementRepository,
                           InstallationMaterialRepository installationMaterialRepository,
                           ApplicationSettingsService applicationSettingsService,
                           UserService userService) {
        this.databaseSettingsService = databaseSettingsService;
        this.managedObjectService = managedObjectService;
        this.customerRepository = customerRepository;
        this.deviceTypeRepository = deviceTypeRepository;
        this.cableTypeRepository = cableTypeRepository;
        this.mountingElementRepository = mountingElementRepository;
        this.installationMaterialRepository = installationMaterialRepository;
        this.applicationSettingsService = applicationSettingsService;
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
        model.addAttribute("mapSettingsForm", MapSettingsForm.from(applicationSettingsService.getMapProvider()));
        model.addAttribute("materialCoefficientsForm", MaterialCoefficientsForm.from(
                applicationSettingsService.getMaterialCoefficients()));
        model.addAttribute("calculationSettingsForm", CalculationSettingsForm.from(
                applicationSettingsService.getStandardCabinetDropLengthMeters()));
        model.addAttribute("currentLogo", applicationSettingsService.getCompanyLogo()
                .map(this::toDataUri)
                .orElse(null));
        model.addAttribute("deviceTypeForm", new DeviceTypeForm());
        model.addAttribute("mountingElementForm", new MountingElementForm());
        model.addAttribute("installationMaterialForm", new InstallationMaterialForm());
        model.addAttribute("deviceTypes", deviceTypeRepository.findAll());
        List<CableType> cableTypes = cableTypeRepository.findAll();
        model.addAttribute("cableTypeForm", new CableTypeForm());
        model.addAttribute("cableTypes", cableTypes);
        model.addAttribute("mountingElements", mountingElementRepository.findAll());
        model.addAttribute("installationMaterials", installationMaterialRepository.findAll());
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

    @PostMapping("/admin/database/{id}/connect")
    public String connectToDatabase(@PathVariable UUID id) {
        databaseSettingsService.activateConnection(id);
        return "redirect:/admin";
    }

    @PostMapping("/admin/database/{id}/disconnect")
    public String disconnectFromDatabase(@PathVariable UUID id) {
        databaseSettingsService.deactivateConnection(id);
        return "redirect:/admin";
    }

    @PostMapping("/admin/database/h2")
    public String createH2(@ModelAttribute("h2Form") H2Form form) {
        databaseSettingsService.createLocalH2Store(form.getName());
        return "redirect:/admin";
    }

    @PostMapping("/admin/objects/{id}/delete")
    public String deleteObject(@PathVariable UUID id, Principal principal) {
        AppUser admin = userService.getByUsername(principal.getName());
        managedObjectService.deletePermanently(id, admin);
        return "redirect:/admin";
    }

    @PostMapping("/admin/objects/{id}/transfer")
    public String transferObject(@PathVariable UUID id, @ModelAttribute("transferForm") TransferForm form, Principal principal) {
        AppUser admin = userService.getByUsername(principal.getName());
        ManagedObject current = managedObjectService.getById(id);
        String name = form.getName() == null || form.getName().isBlank() ? current.getName() : form.getName();
        String description = form.getDescription() == null || form.getDescription().isBlank() ? current.getDescription() : form.getDescription();
        String primaryData = form.getPrimaryData() == null || form.getPrimaryData().isBlank() ? current.getPrimaryData() : form.getPrimaryData();
        managedObjectService.update(id, name, description, primaryData, form.getCustomerId(),
                form.getLatitude(), form.getLongitude(), admin);
        return "redirect:/admin";
    }

    @PostMapping("/admin/users")
    public String createUser(@ModelAttribute("userForm") UserForm form) {
        userService.createUser(form.getUsername(), form.getPassword(), form.isAdmin());
        return "redirect:/admin";
    }

    @PostMapping("/admin/settings/map-provider")
    public String updateMapProvider(@ModelAttribute("mapSettingsForm") MapSettingsForm form) {
        applicationSettingsService.updateMapProvider(form.getMapProvider());
        return "redirect:/admin";
    }

    @PostMapping("/admin/device-types")
    public String createDeviceType(@ModelAttribute("deviceTypeForm") DeviceTypeForm form) {
        if (form.getName() != null && !form.getName().isBlank()) {
            deviceTypeRepository.save(new DeviceType(form.getName().trim()));
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/device-types/{id}/delete")
    public String deleteDeviceType(@PathVariable UUID id) {
        deviceTypeRepository.deleteById(id);
        return "redirect:/admin";
    }

    @PostMapping("/admin/cable-types")
    public String createCableType(@ModelAttribute("cableTypeForm") CableTypeForm form) {
        if (form.getName() != null && !form.getName().isBlank()) {
            String name = form.getName().trim();
            if (!cableTypeRepository.existsByNameIgnoreCase(name)) {
                cableTypeRepository.save(new CableType(name, form.getFunction()));
            }
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/cable-types/{id}/delete")
    public String deleteCableType(@PathVariable UUID id) {
        cableTypeRepository.deleteById(id);
        return "redirect:/admin";
    }

    @PostMapping("/admin/mounting-elements")
    public String createMountingElement(@ModelAttribute("mountingElementForm") MountingElementForm form) {
        if (form.getName() != null && !form.getName().isBlank()) {
            mountingElementRepository.save(new MountingElement(form.getName().trim()));
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/mounting-elements/{id}/delete")
    public String deleteMountingElement(@PathVariable UUID id) {
        mountingElementRepository.deleteById(id);
        return "redirect:/admin";
    }

    @PostMapping("/admin/installation-materials")
    public String createInstallationMaterial(@ModelAttribute("installationMaterialForm") InstallationMaterialForm form) {
        if (form.getName() != null && !form.getName().isBlank()) {
            InstallationMaterial material = new InstallationMaterial(form.getName().trim(),
                    form.getUnit() != null ? form.getUnit().trim() : null);
            installationMaterialRepository.save(material);
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/installation-materials/{id}/delete")
    public String deleteInstallationMaterial(@PathVariable UUID id) {
        installationMaterialRepository.deleteById(id);
        return "redirect:/admin";
    }

    @PostMapping("/admin/settings/material-coefficients")
    public String updateMaterialCoefficients(@ModelAttribute("materialCoefficientsForm") MaterialCoefficientsForm form) {
        applicationSettingsService.updateMaterialCoefficients(
                Math.max(0.0, form.getClipsPerMeter()),
                Math.max(0.0, form.getTiesPerMeter())
        );
        return "redirect:/admin";
    }

    @PostMapping("/admin/settings/calculation")
    public String updateCalculationSettings(@ModelAttribute("calculationSettingsForm") CalculationSettingsForm form) {
        applicationSettingsService.updateStandardCabinetDropLengthMeters(form.getStandardCabinetDropLengthMeters());
        return "redirect:/admin";
    }

    @PostMapping("/admin/settings/company-logo")
    public String uploadCompanyLogo(@RequestParam("logo") MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            try {
                applicationSettingsService.storeCompanyLogo(file.getBytes(), file.getContentType());
            } catch (IOException ex) {
                throw new IllegalStateException("Не удалось сохранить логотип", ex);
            }
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/settings/company-logo/remove")
    public String removeCompanyLogo() {
        applicationSettingsService.removeCompanyLogo();
        return "redirect:/admin";
    }

    private String toDataUri(ApplicationSettingsService.CompanyLogo logo) {
        if (logo == null || logo.data() == null || logo.data().length == 0) {
            return null;
        }
        String contentType = StringUtils.hasText(logo.contentType()) ? logo.contentType() : "image/png";
        String encoded = Base64.getEncoder().encodeToString(logo.data());
        return "data:" + contentType + ";base64," + encoded;
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
        private Double latitude;
        private Double longitude;

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

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
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

    public static class MapSettingsForm {
        @NotNull
        private MapProvider mapProvider = MapProvider.YANDEX;

        public static MapSettingsForm from(MapProvider provider) {
            MapSettingsForm form = new MapSettingsForm();
            form.setMapProvider(provider);
            return form;
        }

        public MapProvider getMapProvider() {
            return mapProvider;
        }

        public void setMapProvider(MapProvider mapProvider) {
            this.mapProvider = mapProvider;
        }
    }

    public static class MaterialCoefficientsForm {
        @PositiveOrZero
        private double clipsPerMeter;
        @PositiveOrZero
        private double tiesPerMeter;

        public static MaterialCoefficientsForm from(ApplicationSettingsService.MaterialCoefficients coefficients) {
            MaterialCoefficientsForm form = new MaterialCoefficientsForm();
            if (coefficients != null) {
                form.setClipsPerMeter(coefficients.clipsPerMeter());
                form.setTiesPerMeter(coefficients.tiesPerMeter());
            }
            return form;
        }

        public double getClipsPerMeter() {
            return clipsPerMeter;
        }

        public void setClipsPerMeter(double clipsPerMeter) {
            this.clipsPerMeter = clipsPerMeter;
        }

        public double getTiesPerMeter() {
            return tiesPerMeter;
        }

        public void setTiesPerMeter(double tiesPerMeter) {
            this.tiesPerMeter = tiesPerMeter;
        }
    }

    public static class CalculationSettingsForm {
        @PositiveOrZero
        private Double standardCabinetDropLengthMeters;

        public static CalculationSettingsForm from(double value) {
            CalculationSettingsForm form = new CalculationSettingsForm();
            form.setStandardCabinetDropLengthMeters(value > 0 ? value : null);
            return form;
        }

        public Double getStandardCabinetDropLengthMeters() {
            return standardCabinetDropLengthMeters;
        }

        public void setStandardCabinetDropLengthMeters(Double standardCabinetDropLengthMeters) {
            this.standardCabinetDropLengthMeters = standardCabinetDropLengthMeters;
        }
    }

    public static class DeviceTypeForm {
        @NotBlank
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class CableTypeForm {
        @NotBlank
        private String name;
        private CableFunction function = CableFunction.SIGNAL;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public CableFunction getFunction() {
            return function;
        }

        public void setFunction(CableFunction function) {
            this.function = function == null ? CableFunction.SIGNAL : function;
        }
    }

    public static class MountingElementForm {
        @NotBlank
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class InstallationMaterialForm {
        @NotBlank
        private String name;
        private String unit;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }
}
