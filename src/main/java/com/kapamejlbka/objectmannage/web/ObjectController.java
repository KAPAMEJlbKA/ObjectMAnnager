package com.kapamejlbka.objectmannage.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kapamejlbka.objectmannage.model.DeviceType;
import com.kapamejlbka.objectmannage.model.InstallationMaterial;
import com.kapamejlbka.objectmannage.model.ManagedObject;
import com.kapamejlbka.objectmannage.model.MapProvider;
import com.kapamejlbka.objectmannage.model.MountingElement;
import com.kapamejlbka.objectmannage.model.ObjectChange;
import com.kapamejlbka.objectmannage.model.PrimaryDataSnapshot;
import com.kapamejlbka.objectmannage.model.ProjectCustomer;
import com.kapamejlbka.objectmannage.model.StoredFile;
import com.kapamejlbka.objectmannage.model.UserAccount;
import com.kapamejlbka.objectmannage.repository.DeviceTypeRepository;
import com.kapamejlbka.objectmannage.repository.InstallationMaterialRepository;
import com.kapamejlbka.objectmannage.repository.MountingElementRepository;
import com.kapamejlbka.objectmannage.repository.ObjectChangeRepository;
import com.kapamejlbka.objectmannage.repository.ProjectCustomerRepository;
import com.kapamejlbka.objectmannage.service.ApplicationSettingsService;
import com.kapamejlbka.objectmannage.service.FilePreviewService;
import com.kapamejlbka.objectmannage.service.ManagedObjectService;
import com.kapamejlbka.objectmannage.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.beans.PropertyEditorSupport;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/objects")
@Validated
public class ObjectController {

    private final ManagedObjectService managedObjectService;
    private final ProjectCustomerRepository customerRepository;
    private final ObjectChangeRepository objectChangeRepository;
    private final DeviceTypeRepository deviceTypeRepository;
    private final MountingElementRepository mountingElementRepository;
    private final InstallationMaterialRepository installationMaterialRepository;
    private final ApplicationSettingsService applicationSettingsService;
    private final UserService userService;
    private final FilePreviewService filePreviewService;
    private final ObjectMapper objectMapper;

    public ObjectController(ManagedObjectService managedObjectService,
                            ProjectCustomerRepository customerRepository,
                            ObjectChangeRepository objectChangeRepository,
                            DeviceTypeRepository deviceTypeRepository,
                            MountingElementRepository mountingElementRepository,
                            InstallationMaterialRepository installationMaterialRepository,
                            ApplicationSettingsService applicationSettingsService,
                            UserService userService,
                            FilePreviewService filePreviewService,
                            ObjectProvider<ObjectMapper> objectMapperProvider) {
        this.managedObjectService = managedObjectService;
        this.customerRepository = customerRepository;
        this.objectChangeRepository = objectChangeRepository;
        this.deviceTypeRepository = deviceTypeRepository;
        this.mountingElementRepository = mountingElementRepository;
        this.installationMaterialRepository = installationMaterialRepository;
        this.applicationSettingsService = applicationSettingsService;
        this.userService = userService;
        this.filePreviewService = filePreviewService;
        this.objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
    }

    @InitBinder
    public void registerUuidTrimmer(WebDataBinder binder) {
        binder.registerCustomEditor(UUID.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (!StringUtils.hasText(text)) {
                    setValue(null);
                    return;
                }
                setValue(UUID.fromString(text.trim()));
            }
        });
    }

    @ModelAttribute("customers")
    public List<ProjectCustomer> customers() {
        return customerRepository.findAll();
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("objects", managedObjectService.listVisibleObjects());
        return "objects/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("form", new ObjectForm());
        return "objects/create";
    }

    @PostMapping
    public String createObject(@Validated @ModelAttribute("form") ObjectForm form,
                               BindingResult bindingResult,
                               Principal principal,
                               Model model) {
        if (form.isCreateNewCustomer()) {
            ObjectCustomerForm newCustomer = form.getNewCustomer();
            if (newCustomer == null || newCustomer.sanitizedName() == null) {
                bindingResult.rejectValue("newCustomer.name", "NotBlank", "Укажите название нового заказчика");
            }
            if (newCustomer != null && newCustomer.sanitizedEmail() != null
                    && !newCustomer.isEmailValid()) {
                bindingResult.rejectValue("newCustomer.contactEmail", "Email", "Некорректный email");
            }
        } else if (form.getCustomerId() == null) {
            bindingResult.rejectValue("customerId", "NotNull", "Выберите заказчика");
        }

        if (bindingResult.hasErrors()) {
            return "objects/create";
        }
        UserAccount user = userService.findByUsername(principal.getName());
        UUID customerId = form.getCustomerId();
        if (form.isCreateNewCustomer()) {
            ObjectCustomerForm newCustomerForm = form.getNewCustomer();
            ProjectCustomer newCustomer = new ProjectCustomer(
                    newCustomerForm.sanitizedName(),
                    newCustomerForm.sanitizedEnterpriseName(),
                    newCustomerForm.sanitizedTaxNumber(),
                    newCustomerForm.sanitizedEmail(),
                    newCustomerForm.sanitizedPhones()
            );
            customerRepository.save(newCustomer);
            customerId = newCustomer.getId();
        }
        ManagedObject managedObject = managedObjectService.create(
                form.getName(),
                form.getDescription(),
                customerId,
                form.getLatitude(),
                form.getLongitude(),
                user
        );
        return "redirect:/objects/" + managedObject.getId();
    }

    @GetMapping("/{id}")
    public String details(@PathVariable UUID id, Model model) {
        ManagedObject managedObject = managedObjectService.getById(id);
        List<ObjectChange> history = objectChangeRepository.findAllByManagedObjectOrderByChangedAtDesc(managedObject);
        MapProvider mapProvider = applicationSettingsService.getMapProvider();
        model.addAttribute("object", managedObject);
        model.addAttribute("history", history);
        model.addAttribute("mapProvider", mapProvider);
        model.addAttribute("coordinateDisplay", formatCoordinates(managedObject.getLatitude(), managedObject.getLongitude()));
        model.addAttribute("mapLink", buildMapLink(mapProvider, managedObject.getLatitude(), managedObject.getLongitude()));
        return "objects/detail";
    }

    @PostMapping("/{id}/files")
    public String uploadFile(@PathVariable UUID id,
                             @RequestParam("file") MultipartFile[] files,
                             Principal principal) {
        UserAccount user = userService.findByUsername(principal.getName());
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                managedObjectService.addFile(id, file, user);
            }
        }
        return "redirect:/objects/" + id;
    }

    @GetMapping("/{objectId}/files/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID objectId,
                                                 @PathVariable UUID fileId,
                                                 @RequestParam(value = "download", defaultValue = "false") boolean download) {
        StoredFile file = managedObjectService.getFile(objectId, fileId);
        Resource resource = managedObjectService.loadFileResource(file);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (StringUtils.hasText(file.getContentType())) {
            try {
                mediaType = MediaType.parseMediaType(file.getContentType());
            } catch (IllegalArgumentException ignored) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
        }

        ContentDisposition disposition = (download ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(file.getOriginalFilename(), StandardCharsets.UTF_8)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDisposition(disposition);
        if (file.getSize() > 0) {
            headers.setContentLength(file.getSize());
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @GetMapping("/{objectId}/files/{fileId}/preview")
    public ResponseEntity<String> previewFile(@PathVariable UUID objectId,
                                              @PathVariable UUID fileId) {
        StoredFile file = managedObjectService.getFile(objectId, fileId);
        String previewHtml = filePreviewService.renderPreview(file);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(previewHtml);
    }

    @PostMapping("/{id}/request-delete")
    public String requestDelete(@PathVariable UUID id, Principal principal) {
        UserAccount user = userService.findByUsername(principal.getName());
        managedObjectService.requestDeletion(id, user);
        return "redirect:/objects";
    }

    @GetMapping("/{id}/primary-data/wizard")
    public String primaryDataWizard(@PathVariable UUID id, Model model) {
        ManagedObject managedObject = managedObjectService.getById(id);
        PrimaryDataWizardForm form = loadPrimaryDataForm(managedObject.getPrimaryData());
        prepareWizardModel(model, managedObject, form);
        return "objects/primary-wizard";
    }

    @PostMapping("/{id}/primary-data/wizard")
    public String savePrimaryData(@PathVariable UUID id,
                                  @ModelAttribute("wizardForm") PrimaryDataWizardForm form,
                                  BindingResult bindingResult,
                                  Principal principal,
                                  Model model) {
        ManagedObject managedObject = managedObjectService.getById(id);
        if (bindingResult.hasErrors()) {
            prepareWizardModel(model, managedObject, form);
            return "objects/primary-wizard";
        }
        List<DeviceType> deviceTypes = deviceTypeRepository.findAll();
        List<MountingElement> mountingElements = mountingElementRepository.findAll();
        List<InstallationMaterial> materials = installationMaterialRepository.findAll();
        PrimaryDataSnapshot snapshot = form.toSnapshot(deviceTypes, mountingElements, materials);
        String json;
        try {
            json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            bindingResult.reject("primaryData", "Не удалось сохранить первичные данные: " + e.getMessage());
            prepareWizardModel(model, managedObject, form);
            return "objects/primary-wizard";
        }
        UserAccount editor = userService.findByUsername(principal.getName());
        managedObjectService.updatePrimaryData(id, json, editor);
        return "redirect:/objects/" + id;
    }

    private PrimaryDataWizardForm loadPrimaryDataForm(String primaryData) {
        List<MountingElement> mountingElements = mountingElementRepository.findAll();
        List<InstallationMaterial> materials = installationMaterialRepository.findAll();
        if (!StringUtils.hasText(primaryData)) {
            return PrimaryDataWizardForm.empty(mountingElements, materials);
        }
        try {
            PrimaryDataSnapshot snapshot = objectMapper.readValue(primaryData, PrimaryDataSnapshot.class);
            return PrimaryDataWizardForm.fromSnapshot(snapshot, mountingElements, materials);
        } catch (JsonProcessingException e) {
            return PrimaryDataWizardForm.empty(mountingElements, materials);
        }
    }

    private void prepareWizardModel(Model model, ManagedObject managedObject, PrimaryDataWizardForm form) {
        List<DeviceType> deviceTypes = deviceTypeRepository.findAll();
        List<MountingElement> mountingElements = mountingElementRepository.findAll();
        List<InstallationMaterial> materials = installationMaterialRepository.findAll();
        model.addAttribute("object", managedObject);
        model.addAttribute("wizardForm", form);
        model.addAttribute("deviceTypes", deviceTypes);
        model.addAttribute("mountingElements", mountingElements);
        model.addAttribute("installationMaterials", materials);
        model.addAttribute("totalConnectionPoints", form.calculateTotalConnectionPoints());
        model.addAttribute("mapProvider", applicationSettingsService.getMapProvider());
    }

    private String formatCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        return String.format("%.6f, %.6f", latitude, longitude);
    }

    private String buildMapLink(MapProvider provider, Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        if (provider == MapProvider.GOOGLE) {
            return String.format("https://www.google.com/maps/search/?api=1&query=%f,%f", latitude, longitude);
        }
        return String.format("https://yandex.by/maps/?ll=%f,%f&z=16&pt=%f,%f", longitude, latitude, longitude, latitude);
    }

    public static class ObjectForm {
        @NotBlank
        private String name;
        private String description;
        private UUID customerId;
        private boolean createNewCustomer;
        private ObjectCustomerForm newCustomer = new ObjectCustomerForm();
        private Double latitude;
        private Double longitude;

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

        public UUID getCustomerId() {
            return customerId;
        }

        public void setCustomerId(UUID customerId) {
            this.customerId = customerId;
        }

        public boolean isCreateNewCustomer() {
            return createNewCustomer;
        }

        public void setCreateNewCustomer(boolean createNewCustomer) {
            this.createNewCustomer = createNewCustomer;
        }

        public ObjectCustomerForm getNewCustomer() {
            if (newCustomer == null) {
                newCustomer = new ObjectCustomerForm();
            }
            return newCustomer;
        }

        public void setNewCustomer(ObjectCustomerForm newCustomer) {
            this.newCustomer = newCustomer;
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

    public static class ObjectCustomerForm {
        private String name;
        private String enterpriseName;
        private String taxNumber;
        private String contactEmail;
        private List<String> contactPhones = new ArrayList<>();

        public ObjectCustomerForm() {
            contactPhones.add("");
        }

        public List<String> sanitizedPhones() {
            if (contactPhones == null) {
                return Collections.emptyList();
            }
            return contactPhones.stream()
                    .map(phone -> phone == null ? null : phone.trim())
                    .filter(phone -> phone != null && !phone.isEmpty())
                    .collect(Collectors.toList());
        }

        public boolean isEmailValid() {
            String email = sanitizedEmail();
            return email == null || email.matches("^[^@]+@[^@]+$");
        }

        public String sanitizedName() {
            return StringUtils.hasText(name) ? name.trim() : null;
        }

        public String sanitizedEnterpriseName() {
            return StringUtils.hasText(enterpriseName) ? enterpriseName.trim() : null;
        }

        public String sanitizedTaxNumber() {
            return StringUtils.hasText(taxNumber) ? taxNumber.trim() : null;
        }

        public String sanitizedEmail() {
            return StringUtils.hasText(contactEmail) ? contactEmail.trim() : null;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEnterpriseName() {
            return enterpriseName;
        }

        public void setEnterpriseName(String enterpriseName) {
            this.enterpriseName = enterpriseName;
        }

        public String getTaxNumber() {
            return taxNumber;
        }

        public void setTaxNumber(String taxNumber) {
            this.taxNumber = taxNumber;
        }

        public String getContactEmail() {
            return contactEmail;
        }

        public void setContactEmail(String contactEmail) {
            this.contactEmail = contactEmail;
        }

        public List<String> getContactPhones() {
            return contactPhones;
        }

        public void setContactPhones(List<String> contactPhones) {
            this.contactPhones = contactPhones == null ? new ArrayList<>() : contactPhones;
            if (this.contactPhones.isEmpty()) {
                this.contactPhones.add("");
            }
        }
    }

    public static class PrimaryDataWizardForm {
        @Valid
        private List<DeviceGroupForm> deviceGroups = new ArrayList<>();
        @Valid
        private List<ConnectionPointForm> connectionPoints = new ArrayList<>();
        @Valid
        private List<MountingSelectionForm> mountingElements = new ArrayList<>();
        @Valid
        private List<MaterialGroupForm> materialGroups = new ArrayList<>();

        public PrimaryDataWizardForm() {
            if (deviceGroups.isEmpty()) {
                deviceGroups.add(new DeviceGroupForm());
            }
            if (connectionPoints.isEmpty()) {
                connectionPoints.add(new ConnectionPointForm());
            }
            if (materialGroups.isEmpty()) {
                materialGroups.add(new MaterialGroupForm());
            }
        }

        public static PrimaryDataWizardForm empty(List<MountingElement> mountingElements,
                                                  List<InstallationMaterial> materials) {
            PrimaryDataWizardForm form = new PrimaryDataWizardForm();
            form.mountingElements = createMountingSelectionForms(Collections.emptyMap(), mountingElements);
            form.connectionPoints = new ArrayList<>();
            form.connectionPoints.add(new ConnectionPointForm());
            ensureMaterialRows(form.materialGroups);
            return form;
        }

        public static PrimaryDataWizardForm fromSnapshot(PrimaryDataSnapshot snapshot,
                                                          List<MountingElement> mountingElements,
                                                          List<InstallationMaterial> materials) {
            PrimaryDataWizardForm form = new PrimaryDataWizardForm();
            form.deviceGroups.clear();
            if (snapshot != null && snapshot.getDeviceGroups() != null) {
                for (PrimaryDataSnapshot.DeviceGroup group : snapshot.getDeviceGroups()) {
                    DeviceGroupForm groupForm = new DeviceGroupForm();
                    groupForm.setDeviceTypeId(group.getDeviceTypeId());
                    groupForm.setDeviceCount(group.getQuantity());
                    groupForm.setInstallLocation(group.getInstallLocation());
                    groupForm.setConnectionPoint(group.getConnectionPoint());
                    groupForm.setDistanceToConnectionPoint(group.getDistanceToConnectionPoint());
                    groupForm.setGroupLabel(group.getGroupLabel());
                    form.deviceGroups.add(groupForm);
                }
            }
            if (form.deviceGroups.isEmpty()) {
                form.deviceGroups.add(new DeviceGroupForm());
            }
            Map<UUID, PrimaryDataSnapshot.MountingRequirement> existingMounting = new HashMap<>();
            if (snapshot != null && snapshot.getMountingElements() != null) {
                for (PrimaryDataSnapshot.MountingRequirement requirement : snapshot.getMountingElements()) {
                    if (requirement.getElementId() != null) {
                        existingMounting.put(requirement.getElementId(), requirement);
                    }
                }
            }
            form.mountingElements = createMountingSelectionForms(existingMounting, mountingElements);

            form.connectionPoints.clear();
            if (snapshot != null && snapshot.getConnectionPoints() != null) {
                for (PrimaryDataSnapshot.ConnectionPoint point : snapshot.getConnectionPoints()) {
                    ConnectionPointForm pointForm = new ConnectionPointForm();
                    pointForm.setName(point.getName());
                    pointForm.setMountingElementId(point.getMountingElementId());
                    form.connectionPoints.add(pointForm);
                }
            }
            if (form.connectionPoints.isEmpty()) {
                List<String> deduped = collectUniqueConnectionPointNames(form.deviceGroups);
                if (deduped.isEmpty()) {
                    form.connectionPoints.add(new ConnectionPointForm());
                } else {
                    for (String name : deduped) {
                        ConnectionPointForm pointForm = new ConnectionPointForm();
                        pointForm.setName(name);
                        form.connectionPoints.add(pointForm);
                    }
                }
            }

            form.materialGroups.clear();
            if (snapshot != null && snapshot.getMaterialGroups() != null) {
                for (PrimaryDataSnapshot.MaterialGroup group : snapshot.getMaterialGroups()) {
                    MaterialGroupForm groupForm = new MaterialGroupForm();
                    groupForm.setGroupName(group.getGroupName());
                    groupForm.setGroupLabel(group.getGroupLabel());
                    groupForm.setSurface(group.getSurface());
                    if (group.getMaterials() != null) {
                        for (PrimaryDataSnapshot.MaterialUsage usage : group.getMaterials()) {
                            MaterialUsageForm usageForm = new MaterialUsageForm();
                            usageForm.setMaterialId(usage.getMaterialId());
                            usageForm.setAmount(usage.getAmount());
                            usageForm.setLayingSurface(usage.getLayingSurface());
                            groupForm.getMaterials().add(usageForm);
                        }
                    }
                    if (groupForm.getMaterials().isEmpty()) {
                        groupForm.getMaterials().add(new MaterialUsageForm());
                    }
                    form.materialGroups.add(groupForm);
                }
            }
            if (form.materialGroups.isEmpty()) {
                form.materialGroups.add(new MaterialGroupForm());
            }
            ensureMaterialRows(form.materialGroups);
            return form;
        }

        private static List<MountingSelectionForm> createMountingSelectionForms(
                Map<UUID, PrimaryDataSnapshot.MountingRequirement> existing,
                List<MountingElement> mountingElements) {
            List<MountingSelectionForm> forms = new ArrayList<>();
            for (MountingElement element : mountingElements) {
                MountingSelectionForm selection = new MountingSelectionForm();
                selection.setElementId(element.getId());
                selection.setElementName(element.getName());
                PrimaryDataSnapshot.MountingRequirement stored = existing.get(element.getId());
                if (stored != null) {
                    selection.setQuantity(stored.getQuantity());
                }
                forms.add(selection);
            }
            return forms;
        }

        private static void ensureMaterialRows(List<MaterialGroupForm> groups) {
            for (MaterialGroupForm group : groups) {
                if (group.getMaterials().isEmpty()) {
                    group.getMaterials().add(new MaterialUsageForm());
                }
            }
        }

        private static List<String> collectUniqueConnectionPointNames(List<DeviceGroupForm> deviceGroups) {
            Set<String> names = new LinkedHashSet<>();
            for (DeviceGroupForm group : deviceGroups) {
                if (group == null) {
                    continue;
                }
                String connectionPoint = group.getConnectionPoint();
                if (StringUtils.hasText(connectionPoint)) {
                    names.add(connectionPoint.trim());
                }
            }
            return new ArrayList<>(names);
        }

        public List<DeviceGroupForm> getDeviceGroups() {
            return deviceGroups;
        }

        public void setDeviceGroups(List<DeviceGroupForm> deviceGroups) {
            this.deviceGroups = deviceGroups == null ? new ArrayList<>() : deviceGroups;
            if (this.deviceGroups.isEmpty()) {
                this.deviceGroups.add(new DeviceGroupForm());
            }
        }

        public List<ConnectionPointForm> getConnectionPoints() {
            return connectionPoints;
        }

        public void setConnectionPoints(List<ConnectionPointForm> connectionPoints) {
            this.connectionPoints = connectionPoints == null ? new ArrayList<>() : connectionPoints;
            if (this.connectionPoints.isEmpty()) {
                this.connectionPoints.add(new ConnectionPointForm());
            }
        }

        public List<MountingSelectionForm> getMountingElements() {
            return mountingElements;
        }

        public void setMountingElements(List<MountingSelectionForm> mountingElements) {
            this.mountingElements = mountingElements == null ? new ArrayList<>() : mountingElements;
        }

        public List<MaterialGroupForm> getMaterialGroups() {
            return materialGroups;
        }

        public void setMaterialGroups(List<MaterialGroupForm> materialGroups) {
            this.materialGroups = materialGroups == null ? new ArrayList<>() : materialGroups;
            if (this.materialGroups.isEmpty()) {
                this.materialGroups.add(new MaterialGroupForm());
            }
            ensureMaterialRows(this.materialGroups);
        }

        public int calculateTotalConnectionPoints() {
            return deviceGroups.stream()
                    .map(DeviceGroupForm::getDeviceCount)
                    .filter(count -> count != null && count > 0)
                    .mapToInt(Integer::intValue)
                    .sum();
        }

        public PrimaryDataSnapshot toSnapshot(List<DeviceType> deviceTypes,
                                              List<MountingElement> availableMountingElements,
                                              List<InstallationMaterial> materials) {
            PrimaryDataSnapshot snapshot = new PrimaryDataSnapshot();
            Map<UUID, DeviceType> deviceTypeMap = deviceTypes.stream()
                    .filter(type -> type.getId() != null)
                    .collect(Collectors.toMap(DeviceType::getId, Function.identity()));
            Map<UUID, MountingElement> elementMap = availableMountingElements.stream()
                    .filter(element -> element.getId() != null)
                    .collect(Collectors.toMap(MountingElement::getId, Function.identity()));
            Map<UUID, InstallationMaterial> materialMap = materials.stream()
                    .filter(material -> material.getId() != null)
                    .collect(Collectors.toMap(InstallationMaterial::getId, Function.identity()));

            List<PrimaryDataSnapshot.DeviceGroup> snapshotGroups = new ArrayList<>();
            for (DeviceGroupForm form : deviceGroups) {
                if (form == null || form.isEmpty()) {
                    continue;
                }
                PrimaryDataSnapshot.DeviceGroup group = new PrimaryDataSnapshot.DeviceGroup();
                group.setDeviceTypeId(form.getDeviceTypeId());
                DeviceType type = form.getDeviceTypeId() != null ? deviceTypeMap.get(form.getDeviceTypeId()) : null;
                group.setDeviceTypeName(type != null ? type.getName() : null);
                group.setQuantity(form.getDeviceCount() != null ? form.getDeviceCount() : 0);
                group.setInstallLocation(trim(form.getInstallLocation()));
                group.setConnectionPoint(trim(form.getConnectionPoint()));
                group.setDistanceToConnectionPoint(form.getDistanceToConnectionPoint());
                group.setGroupLabel(trim(form.getGroupLabel()));
                snapshotGroups.add(group);
            }
            snapshot.setDeviceGroups(snapshotGroups);
            snapshot.setTotalConnectionPoints(calculateTotalConnectionPoints());

            List<PrimaryDataSnapshot.ConnectionPoint> connectionPointSnapshots = new ArrayList<>();
            for (ConnectionPointForm connectionPoint : connectionPoints) {
                if (connectionPoint == null || !StringUtils.hasText(connectionPoint.getName())) {
                    continue;
                }
                PrimaryDataSnapshot.ConnectionPoint snapshotPoint = new PrimaryDataSnapshot.ConnectionPoint();
                snapshotPoint.setName(connectionPoint.getName().trim());
                snapshotPoint.setMountingElementId(connectionPoint.getMountingElementId());
                if (connectionPoint.getMountingElementId() != null) {
                    MountingElement element = elementMap.get(connectionPoint.getMountingElementId());
                    if (element != null) {
                        snapshotPoint.setMountingElementName(element.getName());
                    }
                }
                connectionPointSnapshots.add(snapshotPoint);
            }
            snapshot.setConnectionPoints(connectionPointSnapshots);

            List<PrimaryDataSnapshot.MountingRequirement> requirements = new ArrayList<>();
            for (MountingSelectionForm form : this.mountingElements) {
                if (form == null || !StringUtils.hasText(form.getQuantity())) {
                    continue;
                }
                PrimaryDataSnapshot.MountingRequirement requirement = new PrimaryDataSnapshot.MountingRequirement();
                requirement.setElementId(form.getElementId());
                MountingElement element = form.getElementId() != null ? elementMap.get(form.getElementId()) : null;
                requirement.setElementName(element != null ? element.getName() : form.getElementName());
                requirement.setQuantity(form.getQuantity().trim());
                requirements.add(requirement);
            }
            snapshot.setMountingElements(requirements);

            List<PrimaryDataSnapshot.MaterialGroup> materialGroupsSnapshot = new ArrayList<>();
            for (MaterialGroupForm groupForm : materialGroups) {
                if (groupForm == null) {
                    continue;
                }
                List<PrimaryDataSnapshot.MaterialUsage> usages = new ArrayList<>();
                for (MaterialUsageForm usageForm : groupForm.getMaterials()) {
                    if (usageForm == null || usageForm.isEmpty()) {
                        continue;
                    }
                    PrimaryDataSnapshot.MaterialUsage usage = new PrimaryDataSnapshot.MaterialUsage();
                    usage.setMaterialId(usageForm.getMaterialId());
                    InstallationMaterial material = usageForm.getMaterialId() != null ? materialMap.get(usageForm.getMaterialId()) : null;
                    if (material != null) {
                        usage.setMaterialName(material.getName());
                        usage.setUnit(material.getUnit());
                    }
                    usage.setAmount(trim(usageForm.getAmount()));
                    usage.setLayingSurface(trim(usageForm.getLayingSurface()));
                    usages.add(usage);
                }
                boolean hasGroupData = StringUtils.hasText(groupForm.getGroupName())
                        || StringUtils.hasText(groupForm.getSurface())
                        || !usages.isEmpty();
                if (!hasGroupData) {
                    continue;
                }
                PrimaryDataSnapshot.MaterialGroup group = new PrimaryDataSnapshot.MaterialGroup();
                group.setGroupName(trim(groupForm.getGroupName()));
                group.setGroupLabel(trim(groupForm.getGroupLabel()));
                group.setSurface(trim(groupForm.getSurface()));
                group.setMaterials(usages);
                materialGroupsSnapshot.add(group);
            }
            snapshot.setMaterialGroups(materialGroupsSnapshot);
            return snapshot;
        }

        private String trim(String value) {
            return value == null ? null : value.trim();
        }

        public static class DeviceGroupForm {
            private UUID deviceTypeId;
            private Integer deviceCount;
            private String installLocation;
            private String connectionPoint;
            private Double distanceToConnectionPoint;
            private String groupLabel;

            public boolean isEmpty() {
                return (deviceTypeId == null)
                        && (deviceCount == null || deviceCount == 0)
                        && !StringUtils.hasText(installLocation)
                        && !StringUtils.hasText(connectionPoint)
                        && distanceToConnectionPoint == null
                        && !StringUtils.hasText(groupLabel);
            }

            public UUID getDeviceTypeId() {
                return deviceTypeId;
            }

            public void setDeviceTypeId(UUID deviceTypeId) {
                this.deviceTypeId = deviceTypeId;
            }

            public Integer getDeviceCount() {
                return deviceCount;
            }

            public void setDeviceCount(Integer deviceCount) {
                this.deviceCount = deviceCount;
            }

            public String getInstallLocation() {
                return installLocation;
            }

            public void setInstallLocation(String installLocation) {
                this.installLocation = installLocation;
            }

            public String getConnectionPoint() {
                return connectionPoint;
            }

            public void setConnectionPoint(String connectionPoint) {
                this.connectionPoint = connectionPoint;
            }

            public Double getDistanceToConnectionPoint() {
                return distanceToConnectionPoint;
            }

            public void setDistanceToConnectionPoint(Double distanceToConnectionPoint) {
                this.distanceToConnectionPoint = distanceToConnectionPoint;
            }

            public String getGroupLabel() {
                return groupLabel;
            }

            public void setGroupLabel(String groupLabel) {
                this.groupLabel = groupLabel;
            }
        }

        public static class ConnectionPointForm {
            private String name;
            private UUID mountingElementId;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public UUID getMountingElementId() {
                return mountingElementId;
            }

            public void setMountingElementId(UUID mountingElementId) {
                this.mountingElementId = mountingElementId;
            }
        }

        public static class MountingSelectionForm {
            private UUID elementId;
            private String elementName;
            private String quantity;

            public UUID getElementId() {
                return elementId;
            }

            public void setElementId(UUID elementId) {
                this.elementId = elementId;
            }

            public String getElementName() {
                return elementName;
            }

            public void setElementName(String elementName) {
                this.elementName = elementName;
            }

            public String getQuantity() {
                return quantity;
            }

            public void setQuantity(String quantity) {
                this.quantity = quantity;
            }
        }

        public static class MaterialGroupForm {
            private String groupName;
            private String groupLabel;
            private String surface;
            @Valid
            private List<MaterialUsageForm> materials = new ArrayList<>();

            public String getGroupName() {
                return groupName;
            }

            public void setGroupName(String groupName) {
                this.groupName = groupName;
            }

            public String getGroupLabel() {
                return groupLabel;
            }

            public void setGroupLabel(String groupLabel) {
                this.groupLabel = groupLabel;
            }

            public String getSurface() {
                return surface;
            }

            public void setSurface(String surface) {
                this.surface = surface;
            }

            public List<MaterialUsageForm> getMaterials() {
                return materials;
            }

            public void setMaterials(List<MaterialUsageForm> materials) {
                this.materials = materials == null ? new ArrayList<>() : materials;
                if (this.materials.isEmpty()) {
                    this.materials.add(new MaterialUsageForm());
                }
            }
        }

        public static class MaterialUsageForm {
            private UUID materialId;
            private String amount;
            private String layingSurface;

            public boolean isEmpty() {
                return materialId == null
                        && !StringUtils.hasText(amount)
                        && !StringUtils.hasText(layingSurface);
            }

            public UUID getMaterialId() {
                return materialId;
            }

            public void setMaterialId(UUID materialId) {
                this.materialId = materialId;
            }

            public String getAmount() {
                return amount;
            }

            public void setAmount(String amount) {
                this.amount = amount;
            }

            public String getLayingSurface() {
                return layingSurface;
            }

            public void setLayingSurface(String layingSurface) {
                this.layingSurface = layingSurface;
            }
        }
    }
}
