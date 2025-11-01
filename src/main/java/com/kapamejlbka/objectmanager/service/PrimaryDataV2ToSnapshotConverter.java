package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.model.PrimaryDataSnapshot;
import com.kapamejlbka.objectmanager.model.PrimaryDataSnapshot.ConnectionPoint;
import com.kapamejlbka.objectmanager.model.PrimaryDataSnapshot.DeviceGroup;
import com.kapamejlbka.objectmanager.model.PrimaryDataSnapshot.MaterialGroup;
import com.kapamejlbka.objectmanager.model.PrimaryDataSnapshot.MaterialUsage;
import com.kapamejlbka.objectmanager.model.PrimaryDataSnapshot.Workspace;
import com.kapamejlbka.objectmanager.model.primarydata.PrimaryDataV2;
import com.kapamejlbka.objectmanager.model.primarydata.PrimaryDataV2.DeviceDTO;
import com.kapamejlbka.objectmanager.model.primarydata.PrimaryDataV2.LocationDTO;
import com.kapamejlbka.objectmanager.model.primarydata.PrimaryDataV2.MaterialUsageDTO;
import com.kapamejlbka.objectmanager.model.primarydata.PrimaryDataV2.NodeDTO;
import com.kapamejlbka.objectmanager.model.primarydata.PrimaryDataV2.WorkspaceDTO;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PrimaryDataV2ToSnapshotConverter {

    public PrimaryDataSnapshot convert(PrimaryDataV2 source) {
        PrimaryDataSnapshot snapshot = new PrimaryDataSnapshot();
        if (source == null) {
            return snapshot;
        }
        snapshot.setTotalDeviceCount(source.getTotalDeviceCount());
        snapshot.setTotalNodeCount(source.getTotalNodeCount());
        snapshot.setWorkspaceCount(source.getWorkspaceCount());
        snapshot.setTotalConnectionPoints(defaultInt(source.getTotalConnectionPoints()));
        snapshot.setNodeConnectionMethod(trim(source.getNodeConnectionMethod()));
        snapshot.setNodeConnectionDiagram(trim(source.getNodeConnectionDiagram()));
        snapshot.setMainWorkspaceLocation(trim(source.getMainWorkspaceLocation()));

        Map<UUID, String> nodeNames = new LinkedHashMap<>();
        List<ConnectionPoint> connectionPoints = new ArrayList<>();
        if (source.getNodes() != null) {
            for (NodeDTO node : source.getNodes()) {
                if (node == null) {
                    continue;
                }
                ConnectionPoint point = new ConnectionPoint();
                point.setName(trim(node.getName()));
                point.setMountingElementId(node.getMountingElementId());
                point.setDistanceToPower(node.getDistanceToPower());
                point.setPowerCableTypeId(node.getPowerCableTypeId());
                point.setLayingMaterialId(node.getLayingMaterialId());
                point.setLayingSurface(trim(node.getLayingSurface()));
                point.setLayingSurfaceCategory(trim(node.getLayingSurfaceCategory()));
                point.setSingleSocketCount(node.getSingleSocketCount());
                point.setDoubleSocketCount(node.getDoubleSocketCount());
                point.setBreakerCount(node.getBreakerCount());
                point.setBreakerBoxCount(node.getBreakerBoxCount());
                point.setNshviCount(node.getNshviCount());
                connectionPoints.add(point);
                if (node.getId() != null) {
                    nodeNames.put(node.getId(), point.getName());
                }
            }
        }
        snapshot.setConnectionPoints(connectionPoints);

        Map<UUID, String> locationLabels = new LinkedHashMap<>();
        if (source.getLocations() != null) {
            for (LocationDTO location : source.getLocations()) {
                if (location == null || location.getId() == null) {
                    continue;
                }
                locationLabels.put(location.getId(), trim(location.getLabel()));
            }
        }

        List<DeviceGroup> deviceGroups = new ArrayList<>();
        if (source.getDevices() != null) {
            for (DeviceDTO device : source.getDevices()) {
                if (device == null) {
                    continue;
                }
                DeviceGroup group = new DeviceGroup();
                group.setDeviceTypeId(device.getDeviceTypeId());
                group.setQuantity(defaultInt(device.getQuantity()));
                String locationLabel = resolveLocation(device.getLocationId(), locationLabels);
                group.setInstallLocation(defaultString(device.getInstallLocation(), locationLabel));
                group.setConnectionPoint(resolveNodeName(device.getNodeId(), nodeNames));
                group.setGroupLabel(trim(device.getGroupLabel()));
                group.setDistanceToConnectionPoint(device.getDistanceToNode());
                group.setInstallSurfaceCategory(trim(device.getInstallSurfaceCategory()));
                group.setCameraAccessory(trim(device.getCameraAccessory()));
                group.setCameraViewingDepth(device.getCameraViewingDepth());
                group.setSignalCableTypeId(device.getSignalCableTypeId());
                group.setLowVoltageCableTypeId(device.getLowVoltageCableTypeId());
                deviceGroups.add(group);
            }
        }
        snapshot.setDeviceGroups(deviceGroups);

        Map<String, List<MaterialUsageDTO>> groupedMaterials = new LinkedHashMap<>();
        if (source.getMaterials() != null) {
            for (MaterialUsageDTO dto : source.getMaterials()) {
                if (dto == null) {
                    continue;
                }
                String label = normalizeGroupLabel(dto.getGroupLabel());
                groupedMaterials.computeIfAbsent(label, key -> new ArrayList<>()).add(dto);
            }
        }

        List<MaterialGroup> materialGroups = new ArrayList<>();
        groupedMaterials.forEach((label, entries) -> {
            MaterialGroup group = new MaterialGroup();
            group.setGroupLabel(label);
            List<MaterialUsage> usages = new ArrayList<>();
            for (MaterialUsageDTO dto : entries) {
                MaterialUsage usage = new MaterialUsage();
                usage.setMaterialId(dto.getMaterialId());
                usage.setAmount(trim(dto.getAmount()));
                usage.setUnit(trim(dto.getUnit()));
                usage.setLayingSurface(trim(dto.getLayingSurface()));
                usage.setLayingSurfaceCategory(trim(dto.getLayingSurfaceCategory()));
                usages.add(usage);
            }
            group.setMaterials(usages);
            materialGroups.add(group);
        });
        snapshot.setMaterialGroups(materialGroups);

        List<Workspace> workspaces = new ArrayList<>();
        if (source.getWorkspaces() != null) {
            for (WorkspaceDTO dto : source.getWorkspaces()) {
                if (dto == null) {
                    continue;
                }
                Workspace workspace = new Workspace();
                workspace.setName(trim(dto.getName()));
                workspace.setEquipment(trim(dto.getEquipment()));
                workspace.setLocation(trim(dto.getLocationLabel()));
                workspace.setAssignedNode(resolveNodeName(dto.getNodeId(), nodeNames));
                workspaces.add(workspace);
            }
        }
        snapshot.setWorkspaces(workspaces);

        // V2 mounting requirements are not currently exported in schema, reserved for future use.
        snapshot.setMountingElements(new ArrayList<>());
        return snapshot;
    }

    private String resolveNodeName(UUID nodeId, Map<UUID, String> nodeNames) {
        if (nodeId == null) {
            return null;
        }
        return nodeNames.getOrDefault(nodeId, null);
    }

    private String resolveLocation(UUID locationId, Map<UUID, String> labels) {
        if (locationId == null) {
            return null;
        }
        return labels.getOrDefault(locationId, null);
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String defaultString(String primary, String fallback) {
        if (StringUtils.hasText(primary)) {
            return primary.trim();
        }
        return fallback;
    }

    private String normalizeGroupLabel(String label) {
        if (!StringUtils.hasText(label)) {
            return "Без названия";
        }
        return label.trim();
    }
}
