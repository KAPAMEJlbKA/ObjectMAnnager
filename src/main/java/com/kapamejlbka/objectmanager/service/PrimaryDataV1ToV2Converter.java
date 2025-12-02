package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSnapshot;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSnapshot.ConnectionPoint;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSnapshot.DeviceGroup;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSnapshot.MaterialGroup;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSnapshot.MaterialUsage;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSnapshot.Workspace;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataV2;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataV2.DeviceDTO;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataV2.LocationDTO;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataV2.MaterialUsageDTO;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataV2.NodeDTO;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataV2.WorkspaceDTO;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PrimaryDataV1ToV2Converter {

    public PrimaryDataV2 convert(PrimaryDataSnapshot v1) {
        PrimaryDataV2 v2 = new PrimaryDataV2();
        if (v1 == null) {
            v2.setTotalConnectionPoints(0);
            return v2;
        }
        v2.setTotalDeviceCount(v1.getTotalDeviceCount());
        v2.setTotalNodeCount(v1.getTotalNodeCount());
        v2.setWorkspaceCount(v1.getWorkspaceCount());
        v2.setNodeConnectionMethod(trim(v1.getNodeConnectionMethod()));
        v2.setNodeConnectionDiagram(trim(v1.getNodeConnectionDiagram()));
        v2.setMainWorkspaceLocation(trim(v1.getMainWorkspaceLocation()));

        Map<String, UUID> locationIds = new LinkedHashMap<>();
        Map<String, UUID> nodeIds = new LinkedHashMap<>();
        Map<String, UUID> locationByConnectionPoint = new LinkedHashMap<>();
        List<LocationDTO> locations = new ArrayList<>();
        List<DeviceDTO> devices = new ArrayList<>();

        if (v1.getDeviceGroups() != null) {
            for (DeviceGroup group : v1.getDeviceGroups()) {
                if (group == null) {
                    continue;
                }
                String installLocation = trim(group.getInstallLocation());
                UUID locationId = null;
                if (StringUtils.hasText(installLocation)) {
                    locationId = locationIds.computeIfAbsent(installLocation.toLowerCase(Locale.ROOT),
                            key -> {
                                LocationDTO dto = new LocationDTO();
                                UUID id = deterministicId("location", installLocation);
                                dto.setId(id);
                                dto.setLabel(installLocation);
                                locations.add(dto);
                                return id;
                            });
                }
                String connectionPoint = trim(group.getConnectionPoint());
                if (StringUtils.hasText(connectionPoint) && locationId != null) {
                    locationByConnectionPoint.putIfAbsent(connectionPoint.toLowerCase(Locale.ROOT), locationId);
                }
                DeviceDTO device = new DeviceDTO();
                device.setId(UUID.randomUUID());
                device.setDeviceTypeId(group.getDeviceTypeId());
                device.setLocationId(locationId);
                if (StringUtils.hasText(connectionPoint)) {
                    UUID nodeId = nodeIds.computeIfAbsent(connectionPoint.toLowerCase(Locale.ROOT),
                            key -> deterministicId("node", connectionPoint));
                    device.setNodeId(nodeId);
                }
                device.setQuantity(group.getQuantity());
                device.setInstallLocation(installLocation);
                device.setGroupLabel(trim(group.getGroupLabel()));
                device.setSignalCableTypeId(group.getSignalCableTypeId());
                device.setLowVoltageCableTypeId(group.getLowVoltageCableTypeId());
                device.setDistanceToNode(group.getDistanceToConnectionPoint());
                device.setInstallSurfaceCategory(trim(group.getInstallSurfaceCategory()));
                device.setCameraAccessory(trim(group.getCameraAccessory()));
                device.setCameraViewingDepth(group.getCameraViewingDepth());
                devices.add(device);
            }
        }

        List<NodeDTO> nodes = new ArrayList<>();
        if (v1.getConnectionPoints() != null) {
            for (ConnectionPoint connectionPoint : v1.getConnectionPoints()) {
                if (connectionPoint == null) {
                    continue;
                }
                String name = trim(connectionPoint.getName());
                if (!StringUtils.hasText(name)) {
                    continue;
                }
                UUID nodeId = nodeIds.computeIfAbsent(name.toLowerCase(Locale.ROOT),
                        key -> deterministicId("node", name));
                NodeDTO node = new NodeDTO();
                node.setId(nodeId);
                node.setName(name);
                node.setMountingElementId(connectionPoint.getMountingElementId());
                node.setDistanceToPower(connectionPoint.getDistanceToPower());
                node.setPowerCableTypeId(connectionPoint.getPowerCableTypeId());
                node.setLayingMaterialId(connectionPoint.getLayingMaterialId());
                node.setLayingSurface(trim(connectionPoint.getLayingSurface()));
                node.setLayingSurfaceCategory(trim(connectionPoint.getLayingSurfaceCategory()));
                node.setSingleSocketCount(connectionPoint.getSingleSocketCount());
                node.setDoubleSocketCount(connectionPoint.getDoubleSocketCount());
                node.setBreakerCount(connectionPoint.getBreakerCount());
                node.setBreakerBoxCount(connectionPoint.getBreakerBoxCount());
                node.setNshviCount(connectionPoint.getNshviCount());
                UUID locationId = locationByConnectionPoint.get(name.toLowerCase(Locale.ROOT));
                node.setLocationId(locationId);
                nodes.add(node);
            }
        }

        v2.setTotalConnectionPoints(v1.getTotalConnectionPoints() > 0
                ? v1.getTotalConnectionPoints()
                : nodes.size());

        List<WorkspaceDTO> workspaces = new ArrayList<>();
        if (v1.getWorkspaces() != null) {
            for (Workspace workspace : v1.getWorkspaces()) {
                if (workspace == null) {
                    continue;
                }
                WorkspaceDTO workspaceDTO = new WorkspaceDTO();
                workspaceDTO.setName(trim(workspace.getName()));
                workspaceDTO.setEquipment(trim(workspace.getEquipment()));
                workspaceDTO.setLocationLabel(trim(workspace.getLocation()));
                String assignedNode = trim(workspace.getAssignedNode());
                if (StringUtils.hasText(assignedNode)) {
                    UUID nodeId = nodeIds.computeIfAbsent(assignedNode.toLowerCase(Locale.ROOT),
                            key -> deterministicId("node", assignedNode));
                    workspaceDTO.setNodeId(nodeId);
                }
                workspaces.add(workspaceDTO);
            }
        }

        List<MaterialUsageDTO> materials = new ArrayList<>();
        if (v1.getMaterialGroups() != null) {
            for (MaterialGroup group : v1.getMaterialGroups()) {
                if (group == null || group.getMaterials() == null) {
                    continue;
                }
                String label = trim(group.getGroupLabel());
                for (MaterialUsage usage : group.getMaterials()) {
                    if (usage == null) {
                        continue;
                    }
                    MaterialUsageDTO dto = new MaterialUsageDTO();
                    dto.setMaterialId(usage.getMaterialId());
                    dto.setAmount(trim(usage.getAmount()));
                    dto.setUnit(trim(usage.getUnit()));
                    dto.setLayingSurface(trim(usage.getLayingSurface()));
                    dto.setLayingSurfaceCategory(trim(usage.getLayingSurfaceCategory()));
                    dto.setGroupLabel(label);
                    materials.add(dto);
                }
            }
        }

        v2.setLocations(locations);
        v2.setDevices(devices);
        v2.setNodes(nodes);
        v2.setWorkspaces(workspaces);
        v2.setMaterials(materials);
        return v2;
    }

    private static UUID deterministicId(String namespace, String value) {
        if (!StringUtils.hasText(value)) {
            return UUID.randomUUID();
        }
        String key = namespace + ":" + value.trim().toLowerCase(Locale.ROOT);
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
