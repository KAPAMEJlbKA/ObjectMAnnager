package com.kapamejlbka.objectmanager.web;

import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation;
import com.kapamejlbka.objectmanager.domain.calcengine.CalculationResult;
import com.kapamejlbka.objectmanager.domain.device.EndpointDevice;
import com.kapamejlbka.objectmanager.domain.device.NetworkNode;
import com.kapamejlbka.objectmanager.domain.device.dto.EndpointDeviceCreateRequest;
import com.kapamejlbka.objectmanager.domain.device.dto.NetworkNodeCreateRequest;
import com.kapamejlbka.objectmanager.domain.device.dto.NetworkNodeSettingsForm;
import com.kapamejlbka.objectmanager.domain.device.dto.NetworkNodeSettingsItem;
import com.kapamejlbka.objectmanager.domain.device.dto.NetworkNodeUpdateRequest;
import com.kapamejlbka.objectmanager.domain.topology.InstallationRoute;
import com.kapamejlbka.objectmanager.domain.topology.RouteSegmentLink;
import com.kapamejlbka.objectmanager.domain.topology.TopologyLink;
import com.kapamejlbka.objectmanager.domain.topology.dto.InstallationRouteCreateRequest;
import com.kapamejlbka.objectmanager.domain.topology.dto.RouteSegmentLinkCreateRequest;
import com.kapamejlbka.objectmanager.domain.topology.dto.TopologyLinkCreateRequest;
import com.kapamejlbka.objectmanager.domain.topology.dto.TopologyLinkLengthUpdate;
import com.kapamejlbka.objectmanager.domain.topology.dto.TopologyLinkLengthsForm;
import com.kapamejlbka.objectmanager.service.EndpointDeviceService;
import com.kapamejlbka.objectmanager.service.InstallationRouteService;
import com.kapamejlbka.objectmanager.service.NetworkNodeService;
import com.kapamejlbka.objectmanager.service.RouteSegmentLinkService;
import com.kapamejlbka.objectmanager.service.SystemCalculationService;
import com.kapamejlbka.objectmanager.service.TopologyLinkService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CalculationController {

    private final SystemCalculationService systemCalculationService;
    private final EndpointDeviceService endpointDeviceService;
    private final NetworkNodeService networkNodeService;
    private final TopologyLinkService topologyLinkService;
    private final InstallationRouteService installationRouteService;
    private final RouteSegmentLinkService routeSegmentLinkService;

    public CalculationController(
            SystemCalculationService systemCalculationService,
            EndpointDeviceService endpointDeviceService,
            NetworkNodeService networkNodeService,
            TopologyLinkService topologyLinkService,
            InstallationRouteService installationRouteService,
            RouteSegmentLinkService routeSegmentLinkService) {
        this.systemCalculationService = systemCalculationService;
        this.endpointDeviceService = endpointDeviceService;
        this.networkNodeService = networkNodeService;
        this.topologyLinkService = topologyLinkService;
        this.installationRouteService = installationRouteService;
        this.routeSegmentLinkService = routeSegmentLinkService;
    }

    @GetMapping("/calculations/{id}")
    public String overview(@PathVariable("id") Long id, Model model) {
        SystemCalculation calculation = getCalculation(id);
        model.addAttribute("calculation", calculation);
        return "calculations/overview";
    }

    @GetMapping("/calculations/{id}/wizard/step1")
    public String wizardStep1(@PathVariable("id") Long id, Model model) {
        SystemCalculation calculation = getCalculation(id);
        List<EndpointDevice> devices = endpointDeviceService.listByCalculation(id);
        model.addAttribute("calculation", calculation);
        model.addAttribute("devices", devices);
        model.addAttribute("deviceForm", new EndpointDeviceCreateRequest());
        model.addAttribute("deviceTypes", List.of("CAMERA", "SENSOR", "ACCESS_POINT"));
        model.addAttribute("mountSurfaces", List.of("WALL", "CEILING", "POLE", "UNKNOWN"));
        return "calculations/wizard-step1";
    }

    @PostMapping("/calculations/{id}/wizard/step1/devices")
    public String addDevice(
            @PathVariable("id") Long id,
            @ModelAttribute("deviceForm") EndpointDeviceCreateRequest form,
            RedirectAttributes redirectAttributes) {
        try {
            endpointDeviceService.create(id, form);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        return "redirect:/calculations/" + id + "/wizard/step1";
    }

    @PostMapping("/calculations/{id}/wizard/step1/devices/{deviceId}/delete")
    public String deleteDevice(
            @PathVariable("id") Long id,
            @PathVariable("deviceId") Long deviceId,
            RedirectAttributes redirectAttributes) {
        try {
            endpointDeviceService.delete(deviceId);
            redirectAttributes.addFlashAttribute("flashSuccess", "Устройство удалено");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        return "redirect:/calculations/" + id + "/wizard/step1";
    }

    @GetMapping("/calculations/{id}/wizard/step2")
    public String wizardStep2(@PathVariable("id") Long id, Model model) {
        SystemCalculation calculation = getCalculation(id);
        List<NetworkNode> nodes = networkNodeService.listByCalculation(id);
        NetworkNodeCreateRequest form = new NetworkNodeCreateRequest();
        form.setBaseCircuitBreakers(1);
        form.setBaseSockets(1);
        model.addAttribute("calculation", calculation);
        model.addAttribute("nodes", nodes);
        model.addAttribute("nodeForm", form);
        model.addAttribute("mountSurfaces", List.of("WALL", "CEILING", "POLE", "RACK"));
        return "calculations/wizard-step2";
    }

    @PostMapping("/calculations/{id}/wizard/step2/nodes")
    public String addNode(
            @PathVariable("id") Long id,
            @ModelAttribute("nodeForm") NetworkNodeCreateRequest form,
            RedirectAttributes redirectAttributes) {
        normalizeNodeDefaults(form);
        try {
            networkNodeService.create(id, form);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        return "redirect:/calculations/" + id + "/wizard/step2";
    }

    @GetMapping("/calculations/{id}/wizard/step3")
    public String wizardStep3(@PathVariable("id") Long id, Model model) {
        SystemCalculation calculation = getCalculation(id);
        List<EndpointDevice> devices = endpointDeviceService.listByCalculation(id);
        List<NetworkNode> nodes = networkNodeService.listByCalculation(id);
        List<TopologyLink> links = topologyLinkService.listByCalculation(id);
        model.addAttribute("calculation", calculation);
        model.addAttribute("devices", devices);
        model.addAttribute("nodes", nodes);
        model.addAttribute("links", links);
        model.addAttribute("linkForm", new TopologyLinkCreateRequest());
        model.addAttribute("linkTypes", List.of("UTP", "FIBER", "WIFI", "POWER"));
        return "calculations/wizard-step3";
    }

    @PostMapping("/calculations/{id}/wizard/step3/links")
    public String addLink(
            @PathVariable("id") Long id,
            @ModelAttribute("linkForm") TopologyLinkCreateRequest form,
            @RequestParam("fromEndpoint") String fromEndpoint,
            @RequestParam("toEndpoint") String toEndpoint,
            RedirectAttributes redirectAttributes) {
        try {
            applyEndpointSelection(form, fromEndpoint, true);
            applyEndpointSelection(form, toEndpoint, false);
            topologyLinkService.create(id, form);
            redirectAttributes.addFlashAttribute("flashSuccess", "Связь добавлена");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        return "redirect:/calculations/" + id + "/wizard/step3";
    }

    @PostMapping("/calculations/{id}/wizard/step3/links/{linkId}/delete")
    public String deleteLink(
            @PathVariable("id") Long id,
            @PathVariable("linkId") Long linkId,
            RedirectAttributes redirectAttributes) {
        try {
            topologyLinkService.delete(linkId);
            redirectAttributes.addFlashAttribute("flashSuccess", "Связь удалена");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        return "redirect:/calculations/" + id + "/wizard/step3";
    }

    @GetMapping("/calculations/{id}/wizard/step4")
    public String wizardStep4(@PathVariable("id") Long id, Model model) {
        SystemCalculation calculation = getCalculation(id);
        List<TopologyLink> links = topologyLinkService.listByCalculation(id).stream()
                .filter(l -> List.of("UTP", "FIBER", "POWER").contains(l.getLinkType()))
                .toList();
        TopologyLinkLengthsForm lengthsForm = new TopologyLinkLengthsForm();
        for (TopologyLink link : links) {
            TopologyLinkLengthUpdate entry = new TopologyLinkLengthUpdate();
            entry.setId(link.getId());
            entry.setLength(link.getCableLength());
            lengthsForm.getLinks().add(entry);
        }
        model.addAttribute("calculation", calculation);
        model.addAttribute("links", links);
        model.addAttribute("lengthsForm", lengthsForm);
        return "calculations/wizard-step4";
    }

    @PostMapping("/calculations/{id}/wizard/step4")
    public String updateLengths(
            @PathVariable("id") Long id,
            @ModelAttribute("lengthsForm") TopologyLinkLengthsForm lengthsForm,
            RedirectAttributes redirectAttributes) {
        try {
            topologyLinkService.updateCableLengths(id, lengthsForm.getLinks());
            redirectAttributes.addFlashAttribute("flashSuccess", "Длины линий обновлены");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        return "redirect:/calculations/" + id + "/wizard/step4";
    }

    @GetMapping("/calculations/{id}/wizard/step5")
    public String wizardStep5(@PathVariable("id") Long id, Model model) {
        SystemCalculation calculation = getCalculation(id);
        List<InstallationRoute> routes = installationRouteService.listByCalculation(id);
        Map<Long, List<RouteSegmentLink>> routeLinks = routes.stream()
                .collect(java.util.stream.Collectors.toMap(
                        InstallationRoute::getId, r -> routeSegmentLinkService.listByRoute(r.getId())));
        model.addAttribute("calculation", calculation);
        model.addAttribute("routes", routes);
        model.addAttribute("routeLinks", routeLinks);
        model.addAttribute("topologyLinks", topologyLinkService.listByCalculation(id));
        InstallationRouteCreateRequest routeForm = new InstallationRouteCreateRequest();
        routeForm.setRouteType("CORRUGATED_PIPE");
        model.addAttribute("routeForm", routeForm);
        model.addAttribute("routeTypes", List.of("CORRUGATED_PIPE", "CABLE_CHANNEL", "TRAY_OR_STRUCTURE", "WIRE_ROPE", "BARE_CABLE"));
        model.addAttribute("mountSurfaces", List.of("BETON_OR_BRICK", "METAL", "WOOD", "GYPSUM"));
        model.addAttribute("orientations", List.of("HORIZONTAL", "VERTICAL"));
        model.addAttribute("fixingMethods", List.of("ONE_CLIP", "PE_TIES"));
        model.addAttribute("routeLinkForm", new RouteSegmentLinkCreateRequest());
        return "calculations/wizard-step5";
    }

    @PostMapping("/calculations/{id}/wizard/step5/routes")
    public String addRoute(
            @PathVariable("id") Long id,
            @ModelAttribute("routeForm") InstallationRouteCreateRequest form,
            RedirectAttributes redirectAttributes) {
        try {
            installationRouteService.create(id, form);
            redirectAttributes.addFlashAttribute("flashSuccess", "Трасса добавлена");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        return "redirect:/calculations/" + id + "/wizard/step5";
    }

    @PostMapping("/calculations/{id}/wizard/step5/routes/{routeId}/delete")
    public String deleteRoute(
            @PathVariable("id") Long id,
            @PathVariable("routeId") Long routeId,
            RedirectAttributes redirectAttributes) {
        try {
            installationRouteService.delete(routeId);
            redirectAttributes.addFlashAttribute("flashSuccess", "Трасса удалена");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        return "redirect:/calculations/" + id + "/wizard/step5";
    }

    @PostMapping("/calculations/{id}/wizard/step5/route-links")
    public String addRouteLinks(
            @PathVariable("id") Long id,
            @RequestParam("routeId") Long routeId,
            @RequestParam(value = "topologyLinkIds", required = false) List<Long> topologyLinkIds,
            RedirectAttributes redirectAttributes) {
        try {
            if (topologyLinkIds != null) {
                for (Long linkId : topologyLinkIds) {
                    RouteSegmentLinkCreateRequest dto = new RouteSegmentLinkCreateRequest();
                    dto.setRouteId(routeId);
                    dto.setTopologyLinkId(linkId);
                    routeSegmentLinkService.create(dto);
                }
            }
            redirectAttributes.addFlashAttribute("flashSuccess", "Линии привязаны к трассе");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        return "redirect:/calculations/" + id + "/wizard/step5";
    }

    @GetMapping("/calculations/{id}/wizard/step6")
    public String wizardStep6(@PathVariable("id") Long id, Model model) {
        SystemCalculation calculation = getCalculation(id);
        List<NetworkNode> nodes = networkNodeService.listByCalculation(id);
        NetworkNodeSettingsForm form = new NetworkNodeSettingsForm();
        for (NetworkNode node : nodes) {
            NetworkNodeSettingsItem item = new NetworkNodeSettingsItem();
            item.setId(node.getId());
            item.setCode(node.getCode());
            item.setName(node.getName());
            item.setMountSurface(node.getMountSurface());
            item.setCabinetSize(node.getCabinetSize());
            item.setCabinetSizeAuto(node.getCabinetSizeAuto());
            item.setBaseCircuitBreakers(node.getBaseCircuitBreakers());
            item.setExtraCircuitBreakers(node.getExtraCircuitBreakers());
            item.setBaseSockets(node.getBaseSockets());
            item.setExtraSockets(node.getExtraSockets());
            item.setIncomingLinesCount(node.getIncomingLinesCount());
            form.getNodes().add(item);
        }
        model.addAttribute("calculation", calculation);
        model.addAttribute("nodes", nodes);
        model.addAttribute("nodeSettings", form);
        model.addAttribute("cabinetSizes", List.of(350, 400, 500));
        model.addAttribute("mountSurfaces", List.of("BETON_OR_BRICK", "METAL", "WOOD", "GYPSUM"));
        return "calculations/wizard-step6";
    }

    @PostMapping("/calculations/{id}/wizard/step6")
    public String updateNodes(
            @PathVariable("id") Long id,
            @ModelAttribute("nodeSettings") NetworkNodeSettingsForm form,
            RedirectAttributes redirectAttributes) {
        try {
            for (NetworkNodeSettingsItem item : form.getNodes()) {
                if (item.getId() == null) {
                    continue;
                }
                NetworkNodeUpdateRequest dto = new NetworkNodeUpdateRequest();
                dto.setCode(item.getCode());
                dto.setName(item.getName());
                dto.setMountSurface(item.getMountSurface());
                dto.setCabinetSize(item.getCabinetSize());
                dto.setCabinetSizeAuto(item.getCabinetSizeAuto());
                dto.setBaseCircuitBreakers(item.getBaseCircuitBreakers());
                dto.setExtraCircuitBreakers(item.getExtraCircuitBreakers());
                dto.setBaseSockets(item.getBaseSockets());
                dto.setExtraSockets(item.getExtraSockets());
                dto.setIncomingLinesCount(item.getIncomingLinesCount());
                networkNodeService.update(item.getId(), dto);
            }
            redirectAttributes.addFlashAttribute("flashSuccess", "Параметры узлов сохранены");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        return "redirect:/calculations/" + id + "/wizard/step6";
    }

    @GetMapping("/calculations/{id}/wizard/step7")
    public String wizardStep7(@PathVariable("id") Long id, Model model) {
        SystemCalculation calculation = getCalculation(id);
        CalculationResult result = systemCalculationService.runCalculation(id);
        systemCalculationService.changeStatus(id, "CALCULATED");
        model.addAttribute("calculation", calculation);
        model.addAttribute("result", result);
        return "calculations/wizard-step7";
    }

    private void applyEndpointSelection(TopologyLinkCreateRequest form, String endpoint, boolean isFrom) {
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException("Укажите оба конца линии");
        }
        String[] parts = endpoint.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Некорректный формат точки подключения");
        }
        Long id;
        try {
            id = Long.parseLong(parts[1]);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Некорректный идентификатор точки подключения");
        }
        boolean isNode = "NODE".equalsIgnoreCase(parts[0]);
        if (isFrom) {
            form.setFromNodeId(isNode ? id : null);
            form.setFromDeviceId(isNode ? null : id);
        } else {
            form.setToNodeId(isNode ? id : null);
            form.setToDeviceId(isNode ? null : id);
        }
    }

    private void normalizeNodeDefaults(NetworkNodeCreateRequest form) {
        if (form.getBaseCircuitBreakers() == null || form.getBaseCircuitBreakers() < 1) {
            form.setBaseCircuitBreakers(1);
        }
        if (form.getBaseSockets() == null || form.getBaseSockets() < 1) {
            form.setBaseSockets(1);
        }
        if (form.getCabinetSizeAuto() == null) {
            form.setCabinetSizeAuto(true);
        }
        if (form.getExtraCircuitBreakers() == null) {
            form.setExtraCircuitBreakers(0);
        }
        if (form.getExtraSockets() == null) {
            form.setExtraSockets(0);
        }
    }

    private SystemCalculation getCalculation(Long id) {
        try {
            return systemCalculationService.getById(id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }
}
