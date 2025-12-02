package com.kapamejlbka.objectmanager.legacy.web.form;

import com.kapamejlbka.objectmanager.domain.device.CableFunction;
import com.kapamejlbka.objectmanager.domain.device.CableType;
import com.kapamejlbka.objectmanager.domain.device.DeviceType;
import com.kapamejlbka.objectmanager.domain.device.DeviceTypeRules;
import com.kapamejlbka.objectmanager.domain.device.InstallationMaterial;
import com.kapamejlbka.objectmanager.domain.device.MountingElement;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSnapshot;
import com.kapamejlbka.objectmanager.domain.device.SurfaceType;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

public class PrimaryDataWizardForm {
    private static final Pattern LENGTH_PATTERN = Pattern.compile("(-?\\d+(?:[.,]\\d+)?)");
    private static final double LENGTH_TOLERANCE = 0.0001;
    private static final int NODE_DIAGRAM_MAX_LENGTH = 20000;
    private Integer totalDeviceCount;
    private Integer totalNodeCount;
    private String nodeConnectionMethod;
    private String nodeConnectionDiagram;
    private String mainWorkspaceLocation;
    private Integer workspaceCount;
    @Valid
    private List<DeviceGroupForm> deviceGroups = new ArrayList<>();
    @Valid
    private List<ConnectionPointForm> connectionPoints = new ArrayList<>();
    @Valid
    private List<MountingSelectionForm> mountingElements = new ArrayList<>();
    @Valid
    private List<MaterialGroupForm> materialGroups = new ArrayList<>();
    @Valid
    private List<WorkspaceForm> workspaces = new ArrayList<>();

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
        if (workspaces.isEmpty()) {
            workspaces.add(new WorkspaceForm());
        }
    }

    public static PrimaryDataWizardForm empty(List<MountingElement> mountingElements,
                                              List<InstallationMaterial> materials) {
        PrimaryDataWizardForm form = new PrimaryDataWizardForm();
        form.mountingElements.clear();
        form.connectionPoints = new ArrayList<>();
        form.connectionPoints.add(new ConnectionPointForm());
        ensureWorkspaceRows(form.workspaces);
        ensureMaterialRows(form.materialGroups);
        ensureMountingMaterialRows(form.mountingElements);
        return form;
    }

    public static PrimaryDataWizardForm fromSnapshot(PrimaryDataSnapshot snapshot,
                                                      List<MountingElement> mountingElements,
                                                      List<InstallationMaterial> materials) {
        PrimaryDataWizardForm form = new PrimaryDataWizardForm();
        form.setTotalDeviceCount(snapshot != null ? snapshot.getTotalDeviceCount() : null);
        form.setTotalNodeCount(snapshot != null ? snapshot.getTotalNodeCount() : null);
        form.setNodeConnectionMethod(snapshot != null ? snapshot.getNodeConnectionMethod() : null);
        form.setNodeConnectionDiagram(snapshot != null ? snapshot.getNodeConnectionDiagram() : null);
        form.setMainWorkspaceLocation(snapshot != null ? snapshot.getMainWorkspaceLocation() : null);
        form.setWorkspaceCount(snapshot != null ? snapshot.getWorkspaceCount() : null);
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
                if (requirement.getMaterials() != null) {
                    List<MountingMaterialForm> selectionMaterials = new ArrayList<>();
                    for (PrimaryDataSnapshot.MountingMaterial material : requirement.getMaterials()) {
                        if (material == null) {
                            continue;
                        }
                        MountingMaterialForm materialForm = new MountingMaterialForm();
                        materialForm.setMaterialId(material.getMaterialId());
                        materialForm.setMaterialName(material.getMaterialName());
                        materialForm.setAmount(material.getAmount());
                        selectionMaterials.add(materialForm);
                    }
                    selection.setMaterials(selectionMaterials);
                }
                selection.ensureMaterialRows();
                form.mountingElements.add(selection);
            }
        }
        ensureMountingMaterialRows(form.mountingElements);

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
                Integer singleSockets = point.getSingleSocketCount();
                if (singleSockets != null && singleSockets > 0) {
                    pointForm.setSingleSocketEnabled(true);
                    pointForm.setSingleSocketCount(singleSockets);
                } else {
                    pointForm.setSingleSocketEnabled(false);
                    pointForm.setSingleSocketCount(null);
                }
                Integer doubleSockets = point.getDoubleSocketCount();
                if (doubleSockets != null) {
                    if (doubleSockets > 0) {
                        pointForm.setDoubleSocketEnabled(true);
                        pointForm.setDoubleSocketCount(doubleSockets);
                    } else {
                        pointForm.setDoubleSocketEnabled(false);
                        pointForm.setDoubleSocketCount(1);
                    }
                }
                Integer breakers = point.getBreakerCount();
                if (breakers != null) {
                    if (breakers > 0) {
                        pointForm.setBreakersEnabled(true);
                        pointForm.setBreakerCount(Math.max(breakers, 2));
                    } else {
                        pointForm.setBreakersEnabled(false);
                        pointForm.setBreakerCount(2);
                    }
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

        form.workspaces.clear();
        if (snapshot != null && snapshot.getWorkspaces() != null) {
            for (PrimaryDataSnapshot.Workspace workspace : snapshot.getWorkspaces()) {
                WorkspaceForm workspaceForm = new WorkspaceForm();
                workspaceForm.setName(workspace.getName());
                workspaceForm.setLocation(workspace.getLocation());
                workspaceForm.setEquipment(workspace.getEquipment());
                workspaceForm.setAssignedNode(workspace.getAssignedNode());
                form.workspaces.add(workspaceForm);
            }
        }
        ensureWorkspaceRows(form.workspaces);
        return form;
    }

    private static void ensureMaterialRows(List<MaterialGroupForm> groups) {
        for (MaterialGroupForm group : groups) {
            if (group.getMaterials().isEmpty()) {
                group.getMaterials().add(new MaterialUsageForm());
            }
        }
    }

    private static void ensureMountingMaterialRows(List<MountingSelectionForm> selections) {
        for (MountingSelectionForm selection : selections) {
            if (selection != null) {
                selection.ensureMaterialRows();
            }
        }
    }

    private static void ensureWorkspaceRows(List<WorkspaceForm> workspaces) {
        if (workspaces.isEmpty()) {
            workspaces.add(new WorkspaceForm());
        }
    }

    public void synchronizeMountingSelections(List<MountingElement> availableElements) {
        Map<UUID, MountingElement> elementMap = availableElements == null
                ? Collections.emptyMap()
                : availableElements.stream()
                .filter(element -> element.getId() != null)
                .collect(Collectors.toMap(MountingElement::getId, Function.identity()));
        LinkedHashSet<UUID> assignedIds = new LinkedHashSet<>();
        if (connectionPoints != null) {
            for (ConnectionPointForm point : connectionPoints) {
                if (point == null || point.getMountingElementId() == null) {
                    continue;
                }
                assignedIds.add(point.getMountingElementId());
            }
        }

        Map<UUID, MountingSelectionForm> existingById = new LinkedHashMap<>();
        List<MountingSelectionForm> manualSelections = new ArrayList<>();
        for (MountingSelectionForm selection : new ArrayList<>(mountingElements)) {
            if (selection == null) {
                continue;
            }
            selection.ensureMaterialRows();
            UUID elementId = selection.getElementId();
            if (elementId != null) {
                existingById.putIfAbsent(elementId, selection);
                MountingElement element = elementMap.get(elementId);
                if (element != null) {
                    selection.setElementName(element.getName());
                }
            }
            boolean autoAssigned = elementId != null && assignedIds.contains(elementId);
            selection.setAutoAssigned(autoAssigned);
            if (!autoAssigned) {
                manualSelections.add(selection);
            }
        }

        List<MountingSelectionForm> ordered = new ArrayList<>();
        for (UUID elementId : assignedIds) {
            MountingSelectionForm selection = existingById.get(elementId);
            if (selection == null) {
                selection = new MountingSelectionForm();
                selection.setElementId(elementId);
            }
            selection.setAutoAssigned(true);
            selection.ensureMaterialRows();
            MountingElement element = elementMap.get(elementId);
            if (element != null) {
                selection.setElementName(element.getName());
            }
            ordered.add(selection);
        }

        for (MountingSelectionForm selection : manualSelections) {
            MountingElement element = selection.getElementId() != null ? elementMap.get(selection.getElementId()) : null;
            if (element != null) {
                selection.setElementName(element.getName());
            }
            selection.setAutoAssigned(false);
            ordered.add(selection);
        }

        mountingElements.clear();
        mountingElements.addAll(ordered);
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
        ensureMountingMaterialRows(this.mountingElements);
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

    private int countDefinedConnectionPoints() {
        Set<String> names = new LinkedHashSet<>();
        for (ConnectionPointForm point : connectionPoints) {
            if (point == null) {
                continue;
            }
            String name = point.getName();
            if (StringUtils.hasText(name)) {
                names.add(name.trim());
            }
        }
        return names.size();
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
            DeviceTypeRules.DeviceRequirements requirements = DeviceTypeRules.lookup(typeName).orElse(null);
            if (requirements != null) {
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
            } else {
                if (group.getSignalCableTypeId() == null) {
                    bindingResult.rejectValue(String.format("deviceGroups[%d].signalCableTypeId", groupIndex),
                            "deviceGroups.signalCableTypeId.required",
                            String.format(Locale.getDefault(),
                                    "Выберите сигнальный кабель для \"%s\"",
                                    typeName != null ? typeName : "устройства"));
                }
                if (group.getLowVoltageCableTypeId() == null) {
                    bindingResult.rejectValue(String.format("deviceGroups[%d].lowVoltageCableTypeId", groupIndex),
                            "deviceGroups.lowVoltageCableTypeId.required",
                            String.format(Locale.getDefault(),
                                    "Выберите кабель для слаботочного питания для \"%s\"",
                                    typeName != null ? typeName : "устройства"));
                }
            }

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

        int actualDevices = 0;
        for (DeviceGroupForm group : deviceGroups) {
            if (group != null && group.getDeviceCount() != null) {
                actualDevices += Math.max(group.getDeviceCount(), 0);
            }
        }
        if (totalDeviceCount != null) {
            if (totalDeviceCount < 0) {
                bindingResult.rejectValue("totalDeviceCount", "totalDeviceCount.negative", "Количество устройств не может быть отрицательным");
            } else if (actualDevices > 0 && totalDeviceCount != actualDevices) {
                bindingResult.rejectValue("totalDeviceCount", "totalDeviceCount.mismatch",
                        String.format(Locale.getDefault(), "Указано %d устройств, но по карточкам заполнено %d.", totalDeviceCount, actualDevices));
            }
        }

        int definedNodes = countDefinedConnectionPoints();
        if (totalNodeCount != null) {
            if (totalNodeCount < 0) {
                bindingResult.rejectValue("totalNodeCount", "totalNodeCount.negative", "Количество узлов не может быть отрицательным");
            } else if (definedNodes > 0 && totalNodeCount != definedNodes) {
                bindingResult.rejectValue("totalNodeCount", "totalNodeCount.mismatch",
                        String.format(Locale.getDefault(), "Указано %d узлов, но настроено %d.", totalNodeCount, definedNodes));
            }
        }

        if (workspaceCount != null && workspaceCount < 0) {
            bindingResult.rejectValue("workspaceCount", "workspaceCount.negative", "Количество рабочих мест не может быть отрицательным");
        }

        if (nodeConnectionMethod != null && nodeConnectionMethod.length() > 2000) {
            bindingResult.rejectValue("nodeConnectionMethod", "nodeConnectionMethod.length", "Описание соединения слишком длинное");
        }

        if (nodeConnectionDiagram != null && nodeConnectionDiagram.length() > NODE_DIAGRAM_MAX_LENGTH) {
            bindingResult.rejectValue("nodeConnectionDiagram", "nodeConnectionDiagram.length",
                    String.format(Locale.getDefault(), "Схема содержит слишком много данных (максимум %d символов)", NODE_DIAGRAM_MAX_LENGTH));
        }

        if (mainWorkspaceLocation != null && mainWorkspaceLocation.length() > 255) {
            bindingResult.rejectValue("mainWorkspaceLocation", "mainWorkspaceLocation.length", "Название рабочего места должно быть короче 255 символов");
        }
    }

    public PrimaryDataSnapshot toSnapshot(List<DeviceType> deviceTypes,
                                          List<MountingElement> availableMountingElements,
                                          List<InstallationMaterial> materials,
                                          List<CableType> cableTypes) {
        PrimaryDataSnapshot snapshot = new PrimaryDataSnapshot();
        snapshot.setTotalDeviceCount(getTotalDeviceCount());
        snapshot.setTotalNodeCount(getTotalNodeCount());
        snapshot.setNodeConnectionMethod(trim(nodeConnectionMethod));
        snapshot.setNodeConnectionDiagram(trim(nodeConnectionDiagram));
        snapshot.setMainWorkspaceLocation(trim(mainWorkspaceLocation));
        snapshot.setWorkspaceCount(getWorkspaceCount());
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
            int quantity = form.getDeviceCount() != null && form.getDeviceCount() > 0
                    ? form.getDeviceCount()
                    : 1;
            group.setQuantity(quantity);
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
            connectionPoint.normalizeAccessories();
            int singleSockets = connectionPoint.getEffectiveSingleSocketCount();
            int doubleSockets = connectionPoint.getEffectiveDoubleSocketCount();
            int breakers = connectionPoint.getEffectiveBreakerCount();
            int breakerBoxes = connectionPoint.getBreakerBoxCount();
            int nshvi = connectionPoint.getNshviCount();
            snapshotPoint.setSingleSocketCount(singleSockets > 0 ? singleSockets : null);
            snapshotPoint.setDoubleSocketCount(doubleSockets > 0 ? doubleSockets : null);
            snapshotPoint.setBreakerCount(breakers > 0 ? breakers : null);
            snapshotPoint.setBreakerBoxCount(breakerBoxes > 0 ? breakerBoxes : null);
            snapshotPoint.setNshviCount(nshvi > 0 ? nshvi : null);
            connectionPointSnapshots.add(snapshotPoint);
        }
        snapshot.setConnectionPoints(connectionPointSnapshots);

        List<PrimaryDataSnapshot.MountingRequirement> requirements = new ArrayList<>();
        for (MountingSelectionForm form : this.mountingElements) {
            if (form == null || form.getElementId() == null) {
                continue;
            }
            boolean hasQuantity = StringUtils.hasText(form.getQuantity());
            boolean hasMaterials = form.hasMaterials();
            boolean include = hasQuantity || hasMaterials || form.isAutoAssigned();
            if (!include) {
                continue;
            }
            PrimaryDataSnapshot.MountingRequirement requirement = new PrimaryDataSnapshot.MountingRequirement();
            requirement.setElementId(form.getElementId());
            MountingElement element = elementMap.get(form.getElementId());
            requirement.setElementName(element != null ? element.getName() : form.getElementName());
            requirement.setQuantity(hasQuantity ? form.getQuantity().trim() : null);
            List<PrimaryDataSnapshot.MountingMaterial> materialsForRequirement = new ArrayList<>();
            if (form.getMaterials() != null) {
                for (MountingMaterialForm materialForm : form.getMaterials()) {
                    if (materialForm == null || materialForm.isEmpty()) {
                        continue;
                    }
                    PrimaryDataSnapshot.MountingMaterial material = new PrimaryDataSnapshot.MountingMaterial();
                    material.setMaterialId(materialForm.getMaterialId());
                    InstallationMaterial definedMaterial = materialForm.getMaterialId() != null
                            ? materialMap.get(materialForm.getMaterialId()) : null;
                    if (definedMaterial != null) {
                        material.setMaterialName(definedMaterial.getName());
                        material.setUnit(definedMaterial.getUnit());
                    } else if (StringUtils.hasText(materialForm.getMaterialName())) {
                        material.setMaterialName(materialForm.getMaterialName().trim());
                    }
                    material.setAmount(trim(materialForm.getAmount()));
                    materialsForRequirement.add(material);
                }
            }
            requirement.setMaterials(materialsForRequirement);
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

        List<PrimaryDataSnapshot.Workspace> snapshotWorkspaces = new ArrayList<>();
        for (WorkspaceForm workspaceForm : workspaces) {
            if (workspaceForm == null || workspaceForm.isEmpty()) {
                continue;
            }
            PrimaryDataSnapshot.Workspace workspace = new PrimaryDataSnapshot.Workspace();
            workspace.setName(trim(workspaceForm.getName()));
            workspace.setLocation(trim(workspaceForm.getLocation()));
            workspace.setEquipment(trim(workspaceForm.getEquipment()));
            workspace.setAssignedNode(trim(workspaceForm.getAssignedNode()));
            snapshotWorkspaces.add(workspace);
        }
        snapshot.setWorkspaces(snapshotWorkspaces);
        return snapshot;
    }

    public Integer getTotalDeviceCount() {
        return totalDeviceCount;
    }

    public void setTotalDeviceCount(Integer totalDeviceCount) {
        this.totalDeviceCount = totalDeviceCount;
    }

    public Integer getTotalNodeCount() {
        return totalNodeCount;
    }

    public void setTotalNodeCount(Integer totalNodeCount) {
        this.totalNodeCount = totalNodeCount;
    }

    public String getNodeConnectionMethod() {
        return nodeConnectionMethod;
    }

    public void setNodeConnectionMethod(String nodeConnectionMethod) {
        if (StringUtils.hasText(nodeConnectionMethod)) {
            this.nodeConnectionMethod = nodeConnectionMethod.trim();
        } else {
            this.nodeConnectionMethod = null;
        }
    }

    public String getNodeConnectionDiagram() {
        return nodeConnectionDiagram;
    }

    public void setNodeConnectionDiagram(String nodeConnectionDiagram) {
        if (StringUtils.hasText(nodeConnectionDiagram)) {
            this.nodeConnectionDiagram = nodeConnectionDiagram.trim();
        } else {
            this.nodeConnectionDiagram = null;
        }
    }

    public String getMainWorkspaceLocation() {
        return mainWorkspaceLocation;
    }

    public void setMainWorkspaceLocation(String mainWorkspaceLocation) {
        if (StringUtils.hasText(mainWorkspaceLocation)) {
            this.mainWorkspaceLocation = mainWorkspaceLocation.trim();
        } else {
            this.mainWorkspaceLocation = null;
        }
    }

    public Integer getWorkspaceCount() {
        return workspaceCount;
    }

    public void setWorkspaceCount(Integer workspaceCount) {
        this.workspaceCount = workspaceCount;
    }

    public List<WorkspaceForm> getWorkspaces() {
        return workspaces;
    }

    public void setWorkspaces(List<WorkspaceForm> workspaces) {
        this.workspaces = workspaces;
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


}
