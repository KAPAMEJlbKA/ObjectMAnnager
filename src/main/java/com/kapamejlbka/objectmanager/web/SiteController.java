package com.kapamejlbka.objectmanager.web;

import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation;
import com.kapamejlbka.objectmanager.domain.calculation.dto.SystemCalculationCreateRequest;
import com.kapamejlbka.objectmanager.domain.customer.Customer;
import com.kapamejlbka.objectmanager.domain.customer.Site;
import com.kapamejlbka.objectmanager.domain.customer.dto.SiteCreateRequest;
import com.kapamejlbka.objectmanager.domain.customer.dto.SiteUpdateRequest;
import com.kapamejlbka.objectmanager.service.CustomerService;
import com.kapamejlbka.objectmanager.service.EndpointDeviceService;
import com.kapamejlbka.objectmanager.service.NetworkNodeService;
import com.kapamejlbka.objectmanager.service.SiteService;
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
public class SiteController {

    private final CustomerService customerService;
    private final SiteService siteService;
    private final SystemCalculationService systemCalculationService;
    private final EndpointDeviceService endpointDeviceService;
    private final NetworkNodeService networkNodeService;

    public SiteController(
            CustomerService customerService,
            SiteService siteService,
            SystemCalculationService systemCalculationService,
            EndpointDeviceService endpointDeviceService,
            NetworkNodeService networkNodeService) {
        this.customerService = customerService;
        this.siteService = siteService;
        this.systemCalculationService = systemCalculationService;
        this.endpointDeviceService = endpointDeviceService;
        this.networkNodeService = networkNodeService;
    }

    @GetMapping("/customers/{customerId}/sites/new")
    public String newSiteForm(@PathVariable("customerId") Long customerId, Model model) {
        Customer customer = getCustomer(customerId);
        SiteCreateRequest form = new SiteCreateRequest();
        form.setUseCustomerContact(true);
        populateSiteFormModel(model, customer, form, false, null, null);
        return "sites/form";
    }

    @PostMapping("/customers/{customerId}/sites")
    public String createSite(
            @PathVariable("customerId") Long customerId,
            @ModelAttribute("siteForm") SiteCreateRequest form,
            Model model,
            RedirectAttributes redirectAttributes) {
        Customer customer = getCustomer(customerId);
        try {
            siteService.create(customerId, form);
        } catch (IllegalArgumentException ex) {
            populateSiteFormModel(model, customer, form, false, ex.getMessage(), null);
            return "sites/form";
        }
        redirectAttributes.addFlashAttribute("flashSuccess", "Объект создан");
        return "redirect:/customers/" + customerId;
    }

    @GetMapping("/sites/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Site site = getSite(id);
        List<SystemCalculation> calculations = systemCalculationService.findBySite(id);
        SystemCalculation calculation = calculations.isEmpty() ? null : calculations.get(0);
        Integer deviceCount = null;
        Integer nodeCount = null;
        if (calculation != null) {
            deviceCount = endpointDeviceService.listByCalculation(calculation.getId()).size();
            nodeCount = networkNodeService.listByCalculation(calculation.getId()).size();
        }
        model.addAttribute("site", site);
        model.addAttribute("calculations", calculations);
        model.addAttribute("calculation", calculation);
        model.addAttribute("deviceCount", deviceCount);
        model.addAttribute("nodeCount", nodeCount);
        return "sites/detail";
    }

    @GetMapping("/sites/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Site site = getSite(id);
        SiteUpdateRequest form = new SiteUpdateRequest();
        form.setName(site.getName());
        form.setDescription(site.getDescription());
        form.setFullAddress(site.getFullAddress());
        form.setLatitude(site.getLatitude());
        form.setLongitude(site.getLongitude());
        form.setContactName(site.getContactName());
        form.setContactPosition(site.getContactPosition());
        form.setContactPhone(site.getContactPhone());
        form.setContactEmail(site.getContactEmail());
        form.setUseCustomerContact(site.isUseCustomerContact());
        populateSiteFormModel(model, site.getCustomer(), form, true, null, site.getId());
        return "sites/form";
    }

    @PostMapping("/sites/{id}")
    public String updateSite(
            @PathVariable("id") Long id,
            @ModelAttribute("siteForm") SiteUpdateRequest form,
            Model model,
            RedirectAttributes redirectAttributes) {
        Site site = getSite(id);
        try {
            siteService.update(id, form);
        } catch (IllegalArgumentException ex) {
            populateSiteFormModel(model, site.getCustomer(), form, true, ex.getMessage(), site.getId());
            return "sites/form";
        }
        redirectAttributes.addFlashAttribute("flashSuccess", "Объект обновлён");
        return "redirect:/sites/" + id;
    }

    @GetMapping("/sites/{id}/calculations/new")
    public String newCalculationForm(@PathVariable("id") Long siteId, Model model) {
        return "redirect:/sites/" + siteId + "/calculations/start";
    }

    @PostMapping("/sites/{id}/calculations")
    public String createCalculation(
            @PathVariable("id") Long siteId,
            @ModelAttribute("calculationForm") SystemCalculationCreateRequest form,
            Model model,
            RedirectAttributes redirectAttributes) {
        Site site = getSite(siteId);
        form.setStatus(form.getStatus() == null ? "DRAFT" : form.getStatus());
        SystemCalculation calculation;
        try {
            calculation = systemCalculationService.create(siteId, form);
        } catch (IllegalArgumentException ex) {
            populateCalculationForm(model, site, form, ex.getMessage());
            return "calculations/create";
        }
        redirectAttributes.addFlashAttribute("flashSuccess", "Данные по объекту готовы к заполнению");
        return "redirect:/calculations/" + calculation.getId() + "/wizard/step1";
    }

    @GetMapping("/sites/{id}/calculations/start")
    public String startCalculation(@PathVariable("id") Long siteId, RedirectAttributes redirectAttributes) {
        try {
            SystemCalculation calculation = systemCalculationService.ensureForSite(siteId);
            return "redirect:/calculations/" + calculation.getId() + "/wizard/step1";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
            return "redirect:/sites/" + siteId;
        }
    }

    private void populateSiteFormModel(
            Model model,
            Customer customer,
            Object form,
            boolean isEdit,
            String error,
            Long siteId) {
        model.addAttribute("customer", customer);
        model.addAttribute("siteForm", form);
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("siteId", siteId);
        model.addAttribute("error", error);
    }

    private void populateCalculationForm(Model model, Site site, SystemCalculationCreateRequest form, String error) {
        model.addAttribute("site", site);
        model.addAttribute("calculationForm", form);
        model.addAttribute("error", error);
    }

    private Customer getCustomer(Long id) {
        try {
            return customerService.getById(id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    private Site getSite(Long id) {
        try {
            return siteService.getById(id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }
}
