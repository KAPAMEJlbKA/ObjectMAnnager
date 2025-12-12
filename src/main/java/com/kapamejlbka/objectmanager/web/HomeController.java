package com.kapamejlbka.objectmanager.web;

import com.kapamejlbka.objectmanager.domain.customer.Customer;
import com.kapamejlbka.objectmanager.domain.customer.Site;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class HomeController {

    private final CustomerService customerService;
    private final SiteService siteService;

    public HomeController(CustomerService customerService, SiteService siteService) {
        this.customerService = customerService;
        this.siteService = siteService;
    }

    @GetMapping("/")
    public String home(
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
        return "home";
    }

    @GetMapping("/customers/{id}")
    public String customer(@PathVariable("id") Long id, Model model) {
        Customer customer = getCustomer(id);
        List<Site> sites = siteService.findByCustomer(id);
        model.addAttribute("customer", customer);
        model.addAttribute("sites", sites);
        return "customers/detail";
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
