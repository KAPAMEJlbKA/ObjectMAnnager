package com.kapamejlbka.objectmanager.web;

import com.kapamejlbka.objectmanager.domain.customer.Customer;
import com.kapamejlbka.objectmanager.domain.customer.Site;
import com.kapamejlbka.objectmanager.domain.customer.dto.CustomerCreateRequest;
import com.kapamejlbka.objectmanager.domain.customer.dto.CustomerUpdateRequest;
import com.kapamejlbka.objectmanager.service.CustomerService;
import com.kapamejlbka.objectmanager.service.SiteService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
public class CustomerController {

    private final CustomerService customerService;
    private final SiteService siteService;

    public CustomerController(CustomerService customerService, SiteService siteService) {
        this.customerService = customerService;
        this.siteService = siteService;
    }

    @GetMapping({"/", "/customers"})
    public String list(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            @RequestParam(value = "sort", defaultValue = "name") String sort,
            Model model) {
        List<Customer> customers = new ArrayList<>(customerService.search(query));
        customers.sort(comparatorForSort(sort));

        int pageSize = Math.max(1, size);
        int totalCount = customers.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / pageSize));
        int currentPage = Math.min(Math.max(page, 1), totalPages);
        int fromIndex = Math.min((currentPage - 1) * pageSize, totalCount);
        int toIndex = Math.min(fromIndex + pageSize, totalCount);
        List<Customer> pageItems = customers.subList(fromIndex, toIndex);

        Map<Long, Long> siteCounts = pageItems.stream()
                .collect(Collectors.toMap(Customer::getId, c -> siteService.countByCustomer(c.getId())));

        model.addAttribute("customers", pageItems);
        model.addAttribute("query", query);
        model.addAttribute("sort", sort);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("siteCounts", siteCounts);
        ensureFlashAttributes(model);
        return "customers/list";
    }

    @GetMapping("/customers/new")
    public String newCustomerForm(Model model) {
        populateFormModel(model, new CustomerCreateRequest(), false, null, null);
        return "customers/form";
    }

    @PostMapping("/customers")
    public String createCustomer(
            @ModelAttribute("customerForm") CustomerCreateRequest form,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            customerService.create(form);
        } catch (IllegalArgumentException ex) {
            populateFormModel(model, form, false, ex.getMessage(), null);
            return "customers/form";
        }
        redirectAttributes.addFlashAttribute("flashSuccess", "Заказчик создан");
        return "redirect:/";
    }

    @GetMapping("/customers/{id}")
    public String editCustomer(@PathVariable("id") Long id, Model model) {
        Customer customer = getCustomer(id);
        CustomerUpdateRequest form = toUpdateRequest(customer);
        populateFormModel(model, form, true, null, id);
        return "customers/form";
    }

    @PostMapping("/customers/{id}")
    public String updateCustomer(
            @PathVariable("id") Long id,
            @ModelAttribute("customerForm") CustomerUpdateRequest form,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            customerService.update(id, form);
        } catch (IllegalArgumentException ex) {
            populateFormModel(model, form, true, ex.getMessage(), id);
            return "customers/form";
        }
        redirectAttributes.addFlashAttribute("flashSuccess", "Данные заказчика обновлены");
        return "redirect:/";
    }

    @GetMapping("/customers/{id}/view")
    public String detail(@PathVariable("id") Long id, Model model) {
        Customer customer = getCustomer(id);
        List<Site> sites = siteService.findByCustomer(id);
        model.addAttribute("customer", customer);
        model.addAttribute("sites", sites);
        ensureFlashAttributes(model);
        return "customers/detail";
    }

    private void populateFormModel(
            Model model, CustomerCreateRequest form, boolean isEdit, String error, Long customerId) {
        model.addAttribute("customerForm", form);
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("error", error);
        model.addAttribute("customerId", customerId);
        ensureFlashAttributes(model);
    }

    private void ensureFlashAttributes(Model model) {
        Map<String, Object> attributes = model.asMap();
        if (!attributes.containsKey("flashSuccess")) {
            model.addAttribute("flashSuccess", null);
        }
        if (!attributes.containsKey("flashError")) {
            model.addAttribute("flashError", null);
        }
    }

    private CustomerUpdateRequest toUpdateRequest(Customer customer) {
        CustomerUpdateRequest form = new CustomerUpdateRequest();
        form.setName(customer.getName());
        form.setTaxNumber(customer.getTaxNumber());
        form.setCountry(customer.getCountry());
        form.setCity(customer.getCity());
        form.setAddressLine(customer.getAddressLine());
        form.setContactName(customer.getContactName());
        form.setContactPosition(customer.getContactPosition());
        form.setContactPhone(customer.getContactPhone());
        form.setContactEmail(customer.getContactEmail());
        form.setNotes(customer.getNotes());
        return form;
    }

    private Comparator<Customer> comparatorForSort(String sort) {
        String normalized = sort == null ? "" : sort.trim().toLowerCase();
        return switch (normalized) {
            case "created" -> Comparator.comparing(Customer::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                    .reversed();
            case "updated" -> Comparator.comparing(Customer::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                    .reversed();
            default -> Comparator.comparing(Customer::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        };
    }

    private Customer getCustomer(Long id) {
        try {
            return customerService.getById(id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }
}
