package com.kapamejlbka.objectmannage.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kapamejlbka.objectmannage.model.CableFunction;
import com.kapamejlbka.objectmannage.model.CableType;
import com.kapamejlbka.objectmannage.model.CameraInstallationOption;
import com.kapamejlbka.objectmannage.model.DeviceType;
import com.kapamejlbka.objectmannage.model.DeviceTypeRules;
import com.kapamejlbka.objectmannage.model.InstallationMaterial;
import com.kapamejlbka.objectmannage.model.ManagedObject;
import com.kapamejlbka.objectmannage.model.MapProvider;
import com.kapamejlbka.objectmannage.model.MountingElement;
import com.kapamejlbka.objectmannage.model.ObjectChange;
import com.kapamejlbka.objectmannage.model.PrimaryDataSnapshot;
import com.kapamejlbka.objectmannage.model.PrimaryDataSummary;
import com.kapamejlbka.objectmannage.model.SurfaceType;
import com.kapamejlbka.objectmannage.model.ProjectCustomer;
import com.kapamejlbka.objectmannage.model.StoredFile;
import com.kapamejlbka.objectmannage.model.UserAccount;
import com.kapamejlbka.objectmannage.repository.CableTypeRepository;
import com.kapamejlbka.objectmannage.repository.DeviceTypeRepository;
import com.kapamejlbka.objectmannage.repository.InstallationMaterialRepository;
import com.kapamejlbka.objectmannage.repository.MountingElementRepository;
import com.kapamejlbka.objectmannage.repository.ObjectChangeRepository;
import com.kapamejlbka.objectmannage.repository.ProjectCustomerRepository;
import com.kapamejlbka.objectmannage.service.ApplicationSettingsService;
import com.kapamejlbka.objectmannage.service.FilePreviewService;
import com.kapamejlbka.objectmannage.service.ManagedObjectService;
import com.kapamejlbka.objectmannage.service.PrimaryDataSummaryService;
import com.kapamejlbka.objectmannage.service.PdfReportService;
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
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
    private final CableTypeRepository cableTypeRepository;
    private final ApplicationSettingsService applicationSettingsService;
    private final UserService userService;
    private final FilePreviewService filePreviewService;
    private final ObjectMapper objectMapper;
    private final PrimaryDataSummaryService primaryDataSummaryService;
    private final PdfReportService pdfReportService;

    public ObjectController(ManagedObjectService managedObjectService,
                            ProjectCustomerRepository customerRepository,
                            ObjectChangeRepository objectChangeRepository,
                            DeviceTypeRepository deviceTypeRepository,
                            MountingElementRepository mountingElementRepository,
                            InstallationMaterialRepository installationMaterialRepository,
                            CableTypeRepository cableTypeRepository,
                            ApplicationSettingsService applicationSettingsService,
                            UserService userService,
                            FilePreviewService filePreviewService,
                            ObjectProvider<ObjectMapper> objectMapperProvider,
                            PrimaryDataSummaryService primaryDataSummaryService,
                            PdfReportService pdfReportService) {
        this.managedObjectService = managedObjectService;
        this.customerRepository = customerRepository;
        this.objectChangeRepository = objectChangeRepository;
        this.deviceTypeRepository = deviceTypeRepository;
        this.mountingElementRepository = mountingElementRepository;
        this.installationMaterialRepository = installationMaterialRepository;
        this.cableTypeRepository = cableTypeRepository;
        this.applicationSettingsService = applicationSettingsService;
        this.userService = userService;
        this.filePreviewService = filePreviewService;
        this.objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
        this.primaryDataSummaryService = primaryDataSummaryService;
        this.pdfReportService = pdfReportService;
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
        PrimaryDataSummary primarySummary = primaryDataSummaryService.summarize(managedObject.getPrimaryData());
        model.addAttribute("primarySummary", primarySummary);
        return "objects/detail";
    }

    @GetMapping("/{id}/report.pdf")
    public ResponseEntity<byte[]> downloadReport(@PathVariable UUID id) {
        ManagedObject managedObject = managedObjectService.getById(id);
        PrimaryDataSummary summary = primaryDataSummaryService.summarize(managedObject.getPrimaryData());
        PrimaryDataSnapshot snapshot = parseSnapshot(managedObject.getPrimaryData());
        byte[] pdf = pdfReportService.buildObjectReport(managedObject, snapshot, summary);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(buildReportFileName(managedObject), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(pdf);
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
            prepareWizardModel(model, managedObject, form, determineWizardErrorStep(bindingResult));
            return "objects/primary-wizard";
        }
        List<DeviceType> deviceTypes = deviceTypeRepository.findAll();
        List<MountingElement> mountingElements = mountingElementRepository.findAll();
        List<InstallationMaterial> materials = installationMaterialRepository.findAll();
        List<CableType> cableTypes = cableTypeRepository.findAll();
        form.validate(bindingResult, deviceTypes);
        if (bindingResult.hasErrors()) {
            prepareWizardModel(model, managedObject, form, determineWizardErrorStep(bindingResult));
            return "objects/primary-wizard";
        }
        PrimaryDataSnapshot snapshot = form.toSnapshot(deviceTypes, mountingElements, materials, cableTypes);
        String json;
        try {
            json = objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            bindingResult.reject("primaryData", "Не удалось сохранить первичные данные: " + e.getMessage());
            prepareWizardModel(model, managedObject, form, determineWizardErrorStep(bindingResult));
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
        prepareWizardModel(model, managedObject, form, 0);
    }

    private void prepareWizardModel(Model model,
                                    ManagedObject managedObject,
                                    PrimaryDataWizardForm form,
                                    int activeStep) {
        List<DeviceType> deviceTypes = deviceTypeRepository.findAll();
        List<MountingElement> mountingElements = mountingElementRepository.findAll();
        List<InstallationMaterial> materials = installationMaterialRepository.findAll();
        List<CableType> cableTypes = cableTypeRepository.findAll();
        model.addAttribute("object", managedObject);
        model.addAttribute("wizardForm", form);
        model.addAttribute("deviceTypes", deviceTypes);
        model.addAttribute("mountingElements", mountingElements);
        model.addAttribute("mountingElementOptions", mountingElements);
        model.addAttribute("installationMaterials", materials);
        model.addAttribute("cableTypes", cableTypes);
        model.addAttribute("wizardActiveStep", Math.max(0, Math.min(activeStep, 3)));
        Map<UUID, String> deviceTypeRequirements = deviceTypes.stream()
                .filter(type -> type.getId() != null)
                .collect(Collectors.toMap(DeviceType::getId,
                        type -> DeviceTypeRules.encodeFunctions(DeviceTypeRules.requiredCables(type.getName()))));
        model.addAttribute("deviceTypeRequirements", deviceTypeRequirements);
        model.addAttribute("cableFunctionLabels", DeviceTypeRules.getFunctionLabels());
        model.addAttribute("totalConnectionPoints", form.calculateTotalConnectionPoints());
        model.addAttribute("mapProvider", applicationSettingsService.getMapProvider());
        model.addAttribute("surfaceTypes", SurfaceType.values());
        model.addAttribute("cameraOptions", CameraInstallationOption.values());
    }

    private int determineWizardErrorStep(BindingResult bindingResult) {
        if (bindingResult == null) {
            return 0;
        }
        int step = 0;
        for (FieldError error : bindingResult.getFieldErrors()) {
            String field = error.getField();
            if (field == null) {
                continue;
            }
            if (field.startsWith("materialGroups") || field.startsWith("mountingElements")) {
                step = Math.max(step, 3);
            } else if (field.startsWith("connectionPoints")) {
                step = Math.max(step, 1);
            } else if (field.startsWith("deviceGroups")) {
                if (field.contains("groupLabel")) {
                    step = Math.max(step, 2);
                } else {
                    step = Math.max(step, 0);
                }
            }
        }
        if (step < 3) {
            for (ObjectError error : bindingResult.getGlobalErrors()) {
                String code = error.getCode();
                if (code != null && code.startsWith("materialGroups")) {
                    step = Math.max(step, 3);
                }
            }
        }
        return step;
    }

    private PrimaryDataSnapshot parseSnapshot(String primaryData) {
        if (!StringUtils.hasText(primaryData)) {
            return new PrimaryDataSnapshot();
        }
        try {
            return objectMapper.readValue(primaryData, PrimaryDataSnapshot.class);
        } catch (JsonProcessingException e) {
            return new PrimaryDataSnapshot();
        }
    }

    private String buildReportFileName(ManagedObject managedObject) {
        String objectBase = normalizeFileToken(managedObject != null ? managedObject.getName() : null, "object");
        String customerBase = normalizeFileToken(managedObject != null && managedObject.getCustomer() != null
                ? managedObject.getCustomer().getName() : null, null);
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(customerBase)) {
            builder.append(customerBase).append('-');
        }
        builder.append(objectBase).append("-report.pdf");
        return builder.toString();
    }

    private String normalizeFileToken(String value, String fallback) {
        if (!StringUtils.hasText(value)) {
            return fallback != null ? fallback : null;
        }
        String normalized = value.trim().replaceAll("[^a-zA-Z0-9._-]+", "-");
        if (!StringUtils.hasText(normalized)) {
            return fallback;
        }
        return normalized;
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
        private static final Pattern LENGTH_PATTERN = Pattern.compile("(-?\\d+(?:[.,]\\d+)?)");
        private static final double LENGTH_TOLERANCE = 0.0001;
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
            form.mountingElements.clear();
            form.mountingElements.add(new MountingSelectionForm());
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
                    groupForm.setInstallSurfaceCategory(group.getInstallSurfaceCategory());
                    SurfaceType.resolve(group.getInstallSurfaceCategory())
                            .ifPresent(surfaceType -> groupForm.setInstallSurfaceCategory(surfaceType.getCode()));
                    groupForm.setConnectionPoint(group.getConnectionPoint());
                    groupForm.setDistanceToConnectionPoint(group.getDistanceToConnectionPoint());
                    groupForm.setGroupLabel(group.getGroupLabel());
                    groupForm.setCameraAccessory(group.getCameraAccessory());
                    groupForm.setCameraViewingDepth(group.getCameraViewingDepth());
                    groupForm.setSignalCableTypeId(group.getSignalCableTypeId());
                    groupForm.setLowVoltageCableTypeId(group.getLowVoltageCableTypeId());
                    form.deviceGroups.add(groupForm);
                }
            }
            if (form.deviceGroups.isEmpty()) {
                form.deviceGroups.add(new DeviceGroupForm());
            }
            form.mountingElements.clear();
            if (snapshot != null && snapshot.getMountingElements() != null) {
                for (PrimaryDataSnapshot.MountingRequirement requirement : snapshot.getMountingElements()) {
                    MountingSelectionForm selection = new MountingSelectionForm();
                    selection.setElementId(requirement.getElementId());
                    selection.setElementName(requirement.getElementName());
                    selection.setQuantity(requirement.getQuantity());
                    form.mountingElements.add(selection);
                }
            }
            if (form.mountingElements.isEmpty()) {
                form.mountingElements.add(new MountingSelectionForm());
            }

            form.connectionPoints.clear();
            if (snapshot != null && snapshot.getConnectionPoints() != null) {
                for (PrimaryDataSnapshot.ConnectionPoint point : snapshot.getConnectionPoints()) {
                    ConnectionPointForm pointForm = new ConnectionPointForm();
                    pointForm.setName(point.getName());
                    pointForm.setMountingElementId(point.getMountingElementId());
                    pointForm.setDistanceToPower(point.getDistanceToPower());
                    pointForm.setPowerCableTypeId(point.getPowerCableTypeId());
                    pointForm.setLayingMaterialId(point.getLayingMaterialId());
                    pointForm.setLayingSurface(point.getLayingSurface());
                    pointForm.setLayingSurfaceCategory(point.getLayingSurfaceCategory());
                    if (!StringUtils.hasText(pointForm.getLayingSurfaceCategory())) {
                        SurfaceType.resolve(point.getLayingSurface())
                                .ifPresent(surfaceType -> {
                                    pointForm.setLayingSurfaceCategory(surfaceType.getCode());
                                    pointForm.setLayingSurface(surfaceType.getDisplayName());
                                });
                    } else {
                        SurfaceType.resolve(pointForm.getLayingSurfaceCategory())
                                .ifPresent(surfaceType -> pointForm.setLayingSurface(surfaceType.getDisplayName()));
                    }
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
                    groupForm.setGroupLabel(group.getGroupLabel());
                    if (group.getMaterials() != null) {
                        for (PrimaryDataSnapshot.MaterialUsage usage : group.getMaterials()) {
                            MaterialUsageForm usageForm = new MaterialUsageForm();
                            usageForm.setMaterialId(usage.getMaterialId());
                            usageForm.setAmount(usage.getAmount());
                            usageForm.setLayingSurface(usage.getLayingSurface());
                            usageForm.setLayingSurfaceCategory(usage.getLayingSurfaceCategory());
                            if (!StringUtils.hasText(usageForm.getLayingSurfaceCategory())) {
                                SurfaceType.resolve(usage.getLayingSurface())
                                        .ifPresent(surfaceType -> {
                                            usageForm.setLayingSurfaceCategory(surfaceType.getCode());
                                            usageForm.setLayingSurface(surfaceType.getDisplayName());
                                        });
                            } else {
                                SurfaceType.resolve(usageForm.getLayingSurfaceCategory())
                                        .ifPresent(surfaceType -> {
                                            usageForm.setLayingSurfaceCategory(surfaceType.getCode());
                                            usageForm.setLayingSurface(surfaceType.getDisplayName());
                                        });
                            }
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
            if (this.mountingElements.isEmpty()) {
                this.mountingElements.add(new MountingSelectionForm());
            }
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
            Set<String> unique = new LinkedHashSet<>();
            for (DeviceGroupForm group : deviceGroups) {
                if (group == null) {
                    continue;
                }
                String connectionPoint = group.getConnectionPoint();
                if (StringUtils.hasText(connectionPoint)) {
                    unique.add(connectionPoint.trim());
                }
            }
            return unique.size();
        }

        public void validate(BindingResult bindingResult, List<DeviceType> deviceTypes) {
            Map<UUID, DeviceType> typeMap = deviceTypes == null ? Collections.emptyMap()
                    : deviceTypes.stream()
                    .filter(type -> type != null && type.getId() != null)
                    .collect(Collectors.toMap(DeviceType::getId, Function.identity()));

            Map<String, Double> capacities = new HashMap<>();
            for (int index = 0; index < deviceGroups.size(); index++) {
                DeviceGroupForm group = deviceGroups.get(index);
                if (group == null) {
                    continue;
                }
                final int groupIndex = index;
                DeviceType type = group.getDeviceTypeId() != null ? typeMap.get(group.getDeviceTypeId()) : null;
                String typeName = type != null ? type.getName() : null;
                DeviceTypeRules.lookup(typeName).ifPresent(requirements -> {
                    for (DeviceTypeRules.CableRequirement requirement : requirements.cableRequirements()) {
                        CableFunction function = requirement.function();
                        if (function == CableFunction.SIGNAL && group.getSignalCableTypeId() == null) {
                            bindingResult.rejectValue(String.format("deviceGroups[%d].signalCableTypeId", groupIndex),
                                    "deviceGroups.signalCableTypeId.required",
                                    String.format(Locale.getDefault(),
                                            "Выберите сигнальный кабель для \"%s\"",
                                            typeName != null ? typeName : "устройства"));
                        } else if (function == CableFunction.LOW_VOLTAGE_POWER
                                && group.getLowVoltageCableTypeId() == null) {
                            bindingResult.rejectValue(String.format("deviceGroups[%d].lowVoltageCableTypeId", groupIndex),
                                    "deviceGroups.lowVoltageCableTypeId.required",
                                    String.format(Locale.getDefault(),
                                            "Выберите кабель для слаботочного питания для \"%s\"",
                                            typeName != null ? typeName : "устройства"));
                        }
                    }
                    if (requirements.requireAccessorySelection() && !StringUtils.hasText(group.getCameraAccessory())) {
                        bindingResult.rejectValue(String.format("deviceGroups[%d].cameraAccessory", groupIndex),
                                "deviceGroups.cameraAccessory.required",
                                "Выберите комплектацию для камеры");
                    }
                    if (requirements.requireViewingDepth()) {
                        Double depth = group.getCameraViewingDepth();
                        if (depth == null || depth <= 0) {
                            bindingResult.rejectValue(String.format("deviceGroups[%d].cameraViewingDepth", groupIndex),
                                    "deviceGroups.cameraViewingDepth.required",
                                    "Укажите глубину просмотра для камеры");
                        }
                    }
                });

                String label = trim(group.getGroupLabel());
                if (!StringUtils.hasText(label)) {
                    continue;
                }
                double distance = group.getDistanceToConnectionPoint() != null
                        ? Math.max(0.0, group.getDistanceToConnectionPoint())
                        : 0.0;
                int count = group.getDeviceCount() != null ? Math.max(group.getDeviceCount(), 0) : 0;
                double length = distance * count;
                if (length > 0) {
                    capacities.merge(label, length, Double::sum);
                }
            }
            for (MaterialGroupForm group : materialGroups) {
                if (group == null) {
                    continue;
                }
                String label = trim(group.getGroupLabel());
                if (!StringUtils.hasText(label)) {
                    continue;
                }
                double capacity = capacities.getOrDefault(label, 0.0);
                double used = 0.0;
                if (group.getMaterials() != null) {
                    for (MaterialUsageForm usage : group.getMaterials()) {
                        if (usage == null) {
                            continue;
                        }
                        double length = parseLength(usage.getAmount());
                        if (length > 0) {
                            used += length;
                        }
                    }
                }
                if (capacity > 0 && used > capacity + LENGTH_TOLERANCE) {
                    String message = String.format(Locale.getDefault(),
                            "Группа \"%s\": запланировано %.2f м при доступных %.2f м.",
                            label, used, capacity);
                    bindingResult.reject("materialGroups.capacity", message);
                }
            }
        }

        public PrimaryDataSnapshot toSnapshot(List<DeviceType> deviceTypes,
                                              List<MountingElement> availableMountingElements,
                                              List<InstallationMaterial> materials,
                                              List<CableType> cableTypes) {
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
            Map<UUID, CableType> cableTypeMap = cableTypes.stream()
                    .filter(cable -> cable.getId() != null)
                    .collect(Collectors.toMap(CableType::getId, Function.identity()));

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
                SurfaceType.resolve(form.getInstallSurfaceCategory())
                        .ifPresentOrElse(surfaceType -> group.setInstallSurfaceCategory(surfaceType.getCode()),
                                () -> group.setInstallSurfaceCategory(trim(form.getInstallSurfaceCategory())));
                group.setConnectionPoint(trim(form.getConnectionPoint()));
                group.setDistanceToConnectionPoint(form.getDistanceToConnectionPoint());
                group.setGroupLabel(trim(form.getGroupLabel()));
                if (StringUtils.hasText(form.getCameraAccessory())) {
                    group.setCameraAccessory(form.getCameraAccessory().trim());
                } else {
                    group.setCameraAccessory(null);
                }
                group.setCameraViewingDepth(form.getCameraViewingDepth());
                group.setSignalCableTypeId(form.getSignalCableTypeId());
                if (form.getSignalCableTypeId() != null) {
                    CableType cableType = cableTypeMap.get(form.getSignalCableTypeId());
                    if (cableType != null) {
                        group.setSignalCableTypeName(cableType.getName());
                    }
                }
                group.setLowVoltageCableTypeId(form.getLowVoltageCableTypeId());
                if (form.getLowVoltageCableTypeId() != null) {
                    CableType cableType = cableTypeMap.get(form.getLowVoltageCableTypeId());
                    if (cableType != null) {
                        group.setLowVoltageCableTypeName(cableType.getName());
                    }
                }
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
                snapshotPoint.setDistanceToPower(connectionPoint.getDistanceToPower());
                snapshotPoint.setPowerCableTypeId(connectionPoint.getPowerCableTypeId());
                if (connectionPoint.getPowerCableTypeId() != null) {
                    CableType cableType = cableTypeMap.get(connectionPoint.getPowerCableTypeId());
                    if (cableType != null) {
                        snapshotPoint.setPowerCableTypeName(cableType.getName());
                    }
                }
                snapshotPoint.setLayingMaterialId(connectionPoint.getLayingMaterialId());
                if (connectionPoint.getLayingMaterialId() != null) {
                    InstallationMaterial material = materialMap.get(connectionPoint.getLayingMaterialId());
                    if (material != null) {
                        snapshotPoint.setLayingMaterialName(material.getName());
                        snapshotPoint.setLayingMaterialUnit(material.getUnit());
                    }
                }
                SurfaceType.resolve(connectionPoint.getLayingSurfaceCategory())
                        .ifPresentOrElse(surfaceType -> {
                                    snapshotPoint.setLayingSurface(surfaceType.getDisplayName());
                                    snapshotPoint.setLayingSurfaceCategory(surfaceType.getCode());
                                },
                                () -> {
                                    snapshotPoint.setLayingSurface(trim(connectionPoint.getLayingSurface()));
                                    snapshotPoint.setLayingSurfaceCategory(trim(connectionPoint.getLayingSurfaceCategory()));
                                });
                connectionPointSnapshots.add(snapshotPoint);
            }
            snapshot.setConnectionPoints(connectionPointSnapshots);

            List<PrimaryDataSnapshot.MountingRequirement> requirements = new ArrayList<>();
            for (MountingSelectionForm form : this.mountingElements) {
                if (form == null || form.getElementId() == null || !StringUtils.hasText(form.getQuantity())) {
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
                    SurfaceType.resolve(usageForm.getLayingSurfaceCategory())
                            .ifPresentOrElse(surfaceType -> {
                                usage.setLayingSurface(surfaceType.getDisplayName());
                                usage.setLayingSurfaceCategory(surfaceType.getCode());
                            }, () -> usage.setLayingSurfaceCategory(trim(usageForm.getLayingSurfaceCategory())));
                    usages.add(usage);
                }
                boolean hasGroupData = StringUtils.hasText(groupForm.getGroupLabel())
                        || !usages.isEmpty();
                if (!hasGroupData) {
                    continue;
                }
                PrimaryDataSnapshot.MaterialGroup group = new PrimaryDataSnapshot.MaterialGroup();
                group.setGroupName(trim(groupForm.getGroupLabel()));
                group.setGroupLabel(trim(groupForm.getGroupLabel()));
                group.setMaterials(usages);
                materialGroupsSnapshot.add(group);
            }
            snapshot.setMaterialGroups(materialGroupsSnapshot);
            return snapshot;
        }

        private String trim(String value) {
            return value == null ? null : value.trim();
        }

        private double parseLength(String value) {
            if (!StringUtils.hasText(value)) {
                return 0.0;
            }
            String normalized = value.replace(',', '.');
            Matcher matcher = LENGTH_PATTERN.matcher(normalized);
            if (!matcher.find()) {
                return 0.0;
            }
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException ex) {
                return 0.0;
            }
        }

        public static class DeviceGroupForm {
            private UUID deviceTypeId;
            private Integer deviceCount;
            private String installLocation;
            private String installSurfaceCategory;
            private String connectionPoint;
            private Double distanceToConnectionPoint;
            private String groupLabel;
            private String cameraAccessory;
            private Double cameraViewingDepth;
            private UUID signalCableTypeId;
            private UUID lowVoltageCableTypeId;

            public boolean isEmpty() {
                return (deviceTypeId == null)
                        && (deviceCount == null || deviceCount == 0)
                        && !StringUtils.hasText(installLocation)
                        && !StringUtils.hasText(installSurfaceCategory)
                        && !StringUtils.hasText(connectionPoint)
                        && distanceToConnectionPoint == null
                        && !StringUtils.hasText(groupLabel)
                        && !StringUtils.hasText(cameraAccessory)
                        && cameraViewingDepth == null
                        && signalCableTypeId == null
                        && lowVoltageCableTypeId == null;
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

            public String getInstallSurfaceCategory() {
                return installSurfaceCategory;
            }

            public void setInstallSurfaceCategory(String installSurfaceCategory) {
                this.installSurfaceCategory = installSurfaceCategory;
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

            public String getCameraAccessory() {
                return cameraAccessory;
            }

            public void setCameraAccessory(String cameraAccessory) {
                this.cameraAccessory = cameraAccessory;
            }

            public Double getCameraViewingDepth() {
                return cameraViewingDepth;
            }

            public void setCameraViewingDepth(Double cameraViewingDepth) {
                this.cameraViewingDepth = cameraViewingDepth;
            }

            public UUID getSignalCableTypeId() {
                return signalCableTypeId;
            }

            public void setSignalCableTypeId(UUID signalCableTypeId) {
                this.signalCableTypeId = signalCableTypeId;
            }

            public UUID getLowVoltageCableTypeId() {
                return lowVoltageCableTypeId;
            }

            public void setLowVoltageCableTypeId(UUID lowVoltageCableTypeId) {
                this.lowVoltageCableTypeId = lowVoltageCableTypeId;
            }

        }

        public static class ConnectionPointForm {
            private String name;
            private UUID mountingElementId;
            private Double distanceToPower;
            private UUID powerCableTypeId;
            private UUID layingMaterialId;
            private String layingSurface;
            private String layingSurfaceCategory;

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

            public Double getDistanceToPower() {
                return distanceToPower;
            }

            public void setDistanceToPower(Double distanceToPower) {
                this.distanceToPower = distanceToPower;
            }

            public UUID getPowerCableTypeId() {
                return powerCableTypeId;
            }

            public void setPowerCableTypeId(UUID powerCableTypeId) {
                this.powerCableTypeId = powerCableTypeId;
            }

            public UUID getLayingMaterialId() {
                return layingMaterialId;
            }

            public void setLayingMaterialId(UUID layingMaterialId) {
                this.layingMaterialId = layingMaterialId;
            }

            public String getLayingSurface() {
                return layingSurface;
            }

            public void setLayingSurface(String layingSurface) {
                this.layingSurface = layingSurface;
            }

            public String getLayingSurfaceCategory() {
                return layingSurfaceCategory;
            }

            public void setLayingSurfaceCategory(String layingSurfaceCategory) {
                this.layingSurfaceCategory = layingSurfaceCategory;
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
            private String groupLabel;
            @Valid
            private List<MaterialUsageForm> materials = new ArrayList<>();

            public String getGroupLabel() {
                return groupLabel;
            }

            public void setGroupLabel(String groupLabel) {
                this.groupLabel = groupLabel;
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
            private String layingSurfaceCategory;

            public boolean isEmpty() {
                return materialId == null
                        && !StringUtils.hasText(amount)
                        && !StringUtils.hasText(layingSurface)
                        && !StringUtils.hasText(layingSurfaceCategory);
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

            public String getLayingSurfaceCategory() {
                return layingSurfaceCategory;
            }

            public void setLayingSurfaceCategory(String layingSurfaceCategory) {
                this.layingSurfaceCategory = layingSurfaceCategory;
            }
        }
    }
}
