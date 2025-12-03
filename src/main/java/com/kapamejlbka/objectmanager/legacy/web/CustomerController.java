package com.kapamejlbka.objectmanager.legacy.web;

import com.kapamejlbka.objectmanager.domain.customer.Customer;
import com.kapamejlbka.objectmanager.domain.customer.Site;
import com.kapamejlbka.objectmanager.service.CustomerService;
import com.kapamejlbka.objectmanager.service.SiteService;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final SiteService siteService;

    public CustomerController(CustomerService customerService, SiteService siteService) {
        this.customerService = customerService;
        this.siteService = siteService;
    }

    @GetMapping
    public String list(Model model) {
        List<Customer> customers = customerService.search(null);
        model.addAttribute("customers", customers);
        return "customers/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Customer customer;
        try {
            customer = customerService.getById(id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
        List<Site> sites = safeSites(id);
        model.addAttribute("customer", customer);
        model.addAttribute("sites", sites);
        return "customers/detail";
    }

    private List<Site> safeSites(Long customerId) {
        try {
            return siteService.findByCustomer(customerId);
        } catch (IllegalArgumentException ex) {
            return Collections.emptyList();
        }
    }
}
