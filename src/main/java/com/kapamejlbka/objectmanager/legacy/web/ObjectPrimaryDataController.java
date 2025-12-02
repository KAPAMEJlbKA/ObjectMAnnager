package com.kapamejlbka.objectmanager.legacy.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kapamejlbka.objectmanager.domain.device.CableFunction;
import com.kapamejlbka.objectmanager.domain.device.CableType;
import com.kapamejlbka.objectmanager.domain.device.CameraInstallationOption;
import com.kapamejlbka.objectmanager.domain.device.DeviceType;
import com.kapamejlbka.objectmanager.domain.device.DeviceTypeRules;
import com.kapamejlbka.objectmanager.domain.device.InstallationMaterial;
import com.kapamejlbka.objectmanager.domain.customer.ManagedObject;
import com.kapamejlbka.objectmanager.domain.device.MountingElement;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSnapshot;
import com.kapamejlbka.objectmanager.domain.device.SurfaceType;
import com.kapamejlbka.objectmanager.model.UserAccount;
import com.kapamejlbka.objectmanager.domain.device.repository.CableTypeRepository;
import com.kapamejlbka.objectmanager.domain.device.repository.DeviceTypeRepository;
import com.kapamejlbka.objectmanager.domain.device.repository.InstallationMaterialRepository;
import com.kapamejlbka.objectmanager.domain.device.repository.MountingElementRepository;
import com.kapamejlbka.objectmanager.service.ApplicationSettingsService;
import com.kapamejlbka.objectmanager.service.ManagedObjectService;
import com.kapamejlbka.objectmanager.service.UserService;
import com.kapamejlbka.objectmanager.legacy.web.form.PrimaryDataWizardForm;
import java.beans.PropertyEditorSupport;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;
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

@Controller
@Validated
public class ObjectPrimaryDataController extends ObjectController {

    private final ManagedObjectService managedObjectService;
    private final DeviceTypeRepository deviceTypeRepository;
    private final MountingElementRepository mountingElementRepository;
    private final InstallationMaterialRepository installationMaterialRepository;
    private final CableTypeRepository cableTypeRepository;
    private final ApplicationSettingsService applicationSettingsService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public ObjectPrimaryDataController(ManagedObjectService managedObjectService,
                                       DeviceTypeRepository deviceTypeRepository,
                                       MountingElementRepository mountingElementRepository,
                                       InstallationMaterialRepository installationMaterialRepository,
                                       CableTypeRepository cableTypeRepository,
                                       ApplicationSettingsService applicationSettingsService,
                                       UserService userService,
                                       ObjectProvider<ObjectMapper> objectMapperProvider) {
        this.managedObjectService = managedObjectService;
        this.deviceTypeRepository = deviceTypeRepository;
        this.mountingElementRepository = mountingElementRepository;
        this.installationMaterialRepository = installationMaterialRepository;
        this.cableTypeRepository = cableTypeRepository;
        this.applicationSettingsService = applicationSettingsService;
        this.userService = userService;
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
        form.synchronizeMountingSelections(mountingElements);
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
        PrimaryDataWizardForm form;
        if (!StringUtils.hasText(primaryData)) {
            form = PrimaryDataWizardForm.empty(mountingElements, materials);
        } else {
            try {
                PrimaryDataSnapshot snapshot = objectMapper.readValue(primaryData, PrimaryDataSnapshot.class);
                form = PrimaryDataWizardForm.fromSnapshot(snapshot, mountingElements, materials);
            } catch (JsonProcessingException e) {
                form = PrimaryDataWizardForm.empty(mountingElements, materials);
            }
        }
        form.synchronizeMountingSelections(mountingElements);
        return form;
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
        form.synchronizeMountingSelections(mountingElements);
        model.addAttribute("object", managedObject);
        model.addAttribute("wizardForm", form);
        model.addAttribute("deviceTypes", deviceTypes);
        model.addAttribute("mountingElements", mountingElements);
        model.addAttribute("availableMountingElements", mountingElements);
        model.addAttribute("installationMaterials", materials);
        model.addAttribute("cableTypes", cableTypes);
        model.addAttribute("wizardActiveStep", Math.max(0, Math.min(activeStep, 8)));
        Map<UUID, String> deviceTypeRequirements = deviceTypes.stream()
                .filter(type -> type.getId() != null)
                .collect(Collectors.toMap(DeviceType::getId, type -> {
                    List<DeviceTypeRules.CableRequirement> requirements =
                            DeviceTypeRules.requiredCables(type.getName());
                    if (requirements.isEmpty()) {
                        return String.join(",",
                                CableFunction.SIGNAL.name(),
                                CableFunction.LOW_VOLTAGE_POWER.name());
                    }
                    return DeviceTypeRules.encodeFunctions(requirements);
                }));
        model.addAttribute("deviceTypeRequirements", deviceTypeRequirements);
        Map<CableFunction, String> cableFunctionLabels = DeviceTypeRules.getFunctionLabels();
        model.addAttribute("cableFunctionLabels", cableFunctionLabels);
        model.addAttribute("signalCableFunction", CableFunction.SIGNAL);
        model.addAttribute("lowVoltageCableFunction", CableFunction.LOW_VOLTAGE_POWER);
        model.addAttribute("powerCableFunction", CableFunction.POWER);
        model.addAttribute("totalConnectionPoints", form.calculateTotalConnectionPoints());
        model.addAttribute("mapProvider", applicationSettingsService.getMapProvider());
        model.addAttribute("surfaceTypes", SurfaceType.values());
        model.addAttribute("cameraOptions", CameraInstallationOption.values());
        Map<UUID, Boolean> cameraDeviceFlags = deviceTypes.stream()
                .filter(type -> type.getId() != null)
                .collect(Collectors.toMap(DeviceType::getId,
                        type -> type.getName() != null
                                && type.getName().toLowerCase(Locale.ROOT).contains("камера"),
                        (left, right) -> left,
                        LinkedHashMap::new));
        model.addAttribute("cameraDeviceFlags", cameraDeviceFlags);
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
            if (field.startsWith("totalDeviceCount")
                    || field.startsWith("totalNodeCount")
                    || field.startsWith("workspaceCount")) {
                step = Math.max(step, 0);
            } else if (field.startsWith("nodeConnectionMethod") || field.startsWith("nodeConnectionDiagram")) {
                step = Math.max(step, 4);
            } else if (field.startsWith("mainWorkspaceLocation")) {
                step = Math.max(step, 8);
            } else if (field.startsWith("materialGroups")) {
                step = Math.max(step, 6);
            } else if (field.startsWith("mountingElements")) {
                step = Math.max(step, 7);
            } else if (field.startsWith("connectionPoints")) {
                step = Math.max(step, 7);
            } else if (field.startsWith("workspaces")) {
                if (field.contains("name")) {
                    step = Math.max(step, 3);
                } else {
                    step = Math.max(step, 8);
                }
            } else if (field.startsWith("deviceGroups")) {
                if (field.contains("groupLabel")) {
                    step = Math.max(step, 5);
                } else if (field.contains("connectionPoint")) {
                    step = Math.max(step, 1);
                } else if (field.contains("signalCableType")
                        || field.contains("lowVoltageCableType")
                        || field.contains("distanceToConnectionPoint")
                        || field.contains("installSurface")
                        || field.contains("cameraAccessory")
                        || field.contains("cameraViewingDepth")
                        || field.contains("deviceTypeId")) {
                    step = Math.max(step, 4);
                } else {
                    step = Math.max(step, 1);
                }
            }
        }
        if (step < 6) {
            for (ObjectError error : bindingResult.getGlobalErrors()) {
                String code = error.getCode();
                if (code != null && code.startsWith("materialGroups")) {
                    step = Math.max(step, 6);
                }
            }
        }
        return step;
    }
}
