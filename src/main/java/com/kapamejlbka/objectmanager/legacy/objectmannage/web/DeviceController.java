package com.kapamejlbka.objectmanager.legacy.objectmannage.web;

import com.kapamejlbka.objectmanager.legacy.objectmannage.domain.Device;
import com.kapamejlbka.objectmanager.legacy.objectmannage.domain.DeviceType;
import com.kapamejlbka.objectmanager.legacy.objectmannage.domain.Site;
import com.kapamejlbka.objectmanager.legacy.objectmannage.service.DeviceService;
import com.kapamejlbka.objectmanager.legacy.objectmannage.service.SiteService;
import com.kapamejlbka.objectmanager.legacy.objectmannage.web.form.DeviceForm;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceService deviceService;
    private final SiteService siteService;

    public DeviceController(DeviceService deviceService, SiteService siteService) {
        this.deviceService = deviceService;
        this.siteService = siteService;
    }

    @ModelAttribute("deviceTypes")
    public DeviceType[] deviceTypes() {
        return DeviceType.values();
    }

    @ModelAttribute("sites")
    public List<Site> sites() {
        return siteService.findAllSorted();
    }

    @GetMapping
    public String index(
            @RequestParam(name = "siteId", required = false) UUID siteId,
            @RequestParam(name = "type", required = false) DeviceType type,
            @RequestParam(name = "q", required = false) String query,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model,
            CsrfToken csrfToken
    ) {
        Page<Device> devicesPage = deviceService.findPage(siteId, type, query, pageable);
        model.addAttribute("devicesPage", devicesPage);
        model.addAttribute("selectedSiteId", siteId);
        model.addAttribute("selectedType", type);
        model.addAttribute("q", query);
        model.addAttribute("_csrf", csrfToken);
        return "devices/index";
    }

    @GetMapping("/new")
    public String createForm(Model model, CsrfToken csrfToken) {
        model.addAttribute("form", new DeviceForm());
        model.addAttribute("isEdit", false);
        model.addAttribute("binding", null);
        model.addAttribute("_csrf", csrfToken);
        return "devices/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("form") DeviceForm form,
            BindingResult bindingResult,
            Model model,
            CsrfToken csrfToken,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("binding", bindingResult);
            model.addAttribute("_csrf", csrfToken);
            return "devices/form";
        }

        try {
            deviceService.create(form);
        } catch (EntityNotFoundException ex) {
            bindingResult.addError(new FieldError("form", "siteId", form.getSiteId(), false, null, null, "Выберите площадку"));
            model.addAttribute("isEdit", false);
            model.addAttribute("binding", bindingResult);
            model.addAttribute("_csrf", csrfToken);
            return "devices/form";
        }

        redirectAttributes.addFlashAttribute("flashSuccess", "Сохранено");
        return "redirect:/devices";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") UUID id, Model model, CsrfToken csrfToken) {
        Device device;
        try {
            device = deviceService.get(id);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        model.addAttribute("form", DeviceForm.fromDevice(device));
        model.addAttribute("deviceId", device.getId());
        model.addAttribute("isEdit", true);
        model.addAttribute("binding", null);
        model.addAttribute("_csrf", csrfToken);
        return "devices/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable("id") UUID id,
            @Valid @ModelAttribute("form") DeviceForm form,
            BindingResult bindingResult,
            Model model,
            CsrfToken csrfToken,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("deviceId", id);
            model.addAttribute("binding", bindingResult);
            model.addAttribute("_csrf", csrfToken);
            return "devices/form";
        }

        try {
            deviceService.update(id, form);
        } catch (EntityNotFoundException ex) {
            if (ex.getMessage() != null && ex.getMessage().startsWith("Site not found")) {
                bindingResult.addError(new FieldError("form", "siteId", form.getSiteId(), false, null, null, "Выберите площадку"));
                model.addAttribute("isEdit", true);
                model.addAttribute("deviceId", id);
                model.addAttribute("binding", bindingResult);
                model.addAttribute("_csrf", csrfToken);
                return "devices/form";
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        redirectAttributes.addFlashAttribute("flashSuccess", "Сохранено");
        return "redirect:/devices";
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable("id") UUID id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            deviceService.delete(id);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
            return "redirect:/devices";
        }

        redirectAttributes.addFlashAttribute("flashSuccess", "Удалено");
        return "redirect:/devices";
    }
}
