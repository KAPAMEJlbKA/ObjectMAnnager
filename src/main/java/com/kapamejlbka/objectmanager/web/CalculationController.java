package com.kapamejlbka.objectmanager.web;

import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation;
import com.kapamejlbka.objectmanager.domain.device.EndpointDevice;
import com.kapamejlbka.objectmanager.domain.device.NetworkNode;
import com.kapamejlbka.objectmanager.domain.device.dto.EndpointDeviceCreateRequest;
import com.kapamejlbka.objectmanager.domain.device.dto.NetworkNodeCreateRequest;
import com.kapamejlbka.objectmanager.service.EndpointDeviceService;
import com.kapamejlbka.objectmanager.service.NetworkNodeService;
import com.kapamejlbka.objectmanager.service.SystemCalculationService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CalculationController {

    private final SystemCalculationService systemCalculationService;
    private final EndpointDeviceService endpointDeviceService;
    private final NetworkNodeService networkNodeService;

    public CalculationController(
            SystemCalculationService systemCalculationService,
            EndpointDeviceService endpointDeviceService,
            NetworkNodeService networkNodeService) {
        this.systemCalculationService = systemCalculationService;
        this.endpointDeviceService = endpointDeviceService;
        this.networkNodeService = networkNodeService;
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
