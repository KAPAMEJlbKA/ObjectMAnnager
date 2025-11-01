package com.kapamejlbka.objectmanager.web;

import com.kapamejlbka.objectmanager.model.ProjectCustomer;
import com.kapamejlbka.objectmanager.repository.ProjectCustomerRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/customers")
@Validated
public class CustomerController {

    private final ProjectCustomerRepository customerRepository;

    public CustomerController(ProjectCustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("form", new CustomerForm());
        return "customers/list";
    }

    @PostMapping
    public String createCustomer(@ModelAttribute("form") CustomerForm form) {
        List<String> phones = form.getContactPhones().stream()
                .map(phone -> phone == null ? null : phone.trim())
                .filter(phone -> phone != null && !phone.isEmpty())
                .collect(Collectors.toList());
        ProjectCustomer customer = new ProjectCustomer(
                form.getName(),
                form.getEnterpriseName(),
                form.getTaxNumber(),
                form.getContactEmail(),
                phones
        );
        customerRepository.save(customer);
        return "redirect:/customers";
    }

    public static class CustomerForm {
        @NotBlank
        private String name;
        @Email(message = "Некорректный email", regexp = "^$|^[^@]+@[^@]+$")
        private String contactEmail;
        private String enterpriseName;
        private String taxNumber;
        private List<String> contactPhones = new ArrayList<>();

        public CustomerForm() {
            this.contactPhones.add("");
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContactEmail() {
            return contactEmail;
        }

        public void setContactEmail(String contactEmail) {
            this.contactEmail = contactEmail;
        }

        public String getEnterpriseName() {
            return enterpriseName;
        }

        public void setEnterpriseName(String enterpriseName) {
            this.enterpriseName = enterpriseName;
        }

        public String getTaxNumber() {
            return taxNumber;
        }

        public void setTaxNumber(String taxNumber) {
            this.taxNumber = taxNumber;
        }

        public List<String> getContactPhones() {
            return contactPhones;
        }

        public void setContactPhones(List<String> contactPhones) {
            this.contactPhones = contactPhones;
        }
    }
}
