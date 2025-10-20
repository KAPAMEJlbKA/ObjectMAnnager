package com.kapamejlbka.objectmannage.web;

import com.kapamejlbka.objectmannage.model.ProjectCustomer;
import com.kapamejlbka.objectmannage.repository.ProjectCustomerRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
        ProjectCustomer customer = new ProjectCustomer(form.getName(), form.getContactEmail(), form.getContactPhone());
        customerRepository.save(customer);
        return "redirect:/customers";
    }

    public static class CustomerForm {
        @NotBlank
        private String name;
        @Email(message = "Некорректный email", regexp = "^$|^[^@]+@[^@]+$")
        private String contactEmail;
        private String contactPhone;

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

        public String getContactPhone() {
            return contactPhone;
        }

        public void setContactPhone(String contactPhone) {
            this.contactPhone = contactPhone;
        }
    }
}
