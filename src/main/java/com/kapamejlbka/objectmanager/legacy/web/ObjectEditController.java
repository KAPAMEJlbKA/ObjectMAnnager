package com.kapamejlbka.objectmanager.legacy.web;

import com.kapamejlbka.objectmanager.domain.customer.ManagedObject;
import com.kapamejlbka.objectmanager.domain.customer.ProjectCustomer;
import com.kapamejlbka.objectmanager.model.UserAccount;
import com.kapamejlbka.objectmanager.domain.customer.repository.ProjectCustomerRepository;
import com.kapamejlbka.objectmanager.service.ManagedObjectService;
import com.kapamejlbka.objectmanager.service.UserService;
import com.kapamejlbka.objectmanager.legacy.web.form.ObjectCustomerForm;
import com.kapamejlbka.objectmanager.legacy.web.form.ObjectForm;
import java.beans.PropertyEditorSupport;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@Validated
public class ObjectEditController extends ObjectController {

    private final ManagedObjectService managedObjectService;
    private final ProjectCustomerRepository customerRepository;
    private final UserService userService;

    public ObjectEditController(ManagedObjectService managedObjectService,
                                ProjectCustomerRepository customerRepository,
                                UserService userService) {
        this.managedObjectService = managedObjectService;
        this.customerRepository = customerRepository;
        this.userService = userService;
    }

    @InitBinder
    public void registerUuidTrimmer(WebDataBinder binder) {
        binder.registerCustomEditor(UUID.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (!StringUtils.hasText(text)) {
                    setValue(null);
                    return;
                }
                setValue(UUID.fromString(text.trim()));
            }
        });
    }

    @ModelAttribute("customers")
    public List<ProjectCustomer> customers() {
        return customerRepository.findAll();
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("form", new ObjectForm());
        return "objects/create";
    }

    @PostMapping
    public String createObject(@Validated @ModelAttribute("form") ObjectForm form,
                               BindingResult bindingResult,
                               Principal principal) {
        if (form.isCreateNewCustomer()) {
            ObjectCustomerForm newCustomer = form.getNewCustomer();
            if (newCustomer == null || newCustomer.sanitizedName() == null) {
                bindingResult.rejectValue("newCustomer.name", "NotBlank", "Укажите название нового заказчика");
            }
            if (newCustomer != null && newCustomer.sanitizedEmail() != null
                    && !newCustomer.isEmailValid()) {
                bindingResult.rejectValue("newCustomer.contactEmail", "Email", "Некорректный email");
            }
        } else if (form.getCustomerId() == null) {
            bindingResult.rejectValue("customerId", "NotNull", "Выберите заказчика");
        }

        if (bindingResult.hasErrors()) {
            return "objects/create";
        }
        UserAccount user = userService.findByUsername(principal.getName());
        UUID customerId = form.getCustomerId();
        if (form.isCreateNewCustomer()) {
            ObjectCustomerForm newCustomerForm = form.getNewCustomer();
            ProjectCustomer newCustomer = new ProjectCustomer(
                    newCustomerForm.sanitizedName(),
                    newCustomerForm.sanitizedEnterpriseName(),
                    newCustomerForm.sanitizedTaxNumber(),
                    newCustomerForm.sanitizedEmail(),
                    newCustomerForm.sanitizedPhones()
            );
            customerRepository.save(newCustomer);
            customerId = newCustomer.getId();
        }
        ManagedObject managedObject = managedObjectService.create(
                form.getName(),
                form.getDescription(),
                customerId,
                form.getLatitude(),
                form.getLongitude(),
                user
        );
        return "redirect:/objects/" + managedObject.getId();
    }

    @PostMapping("/{id}/request-delete")
    public String requestDelete(@PathVariable UUID id, Principal principal) {
        UserAccount user = userService.findByUsername(principal.getName());
        managedObjectService.requestDeletion(id, user);
        return "redirect:/objects";
    }
}
