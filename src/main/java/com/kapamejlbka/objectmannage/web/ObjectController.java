package com.kapamejlbka.objectmannage.web;

import com.kapamejlbka.objectmannage.model.ManagedObject;
import com.kapamejlbka.objectmannage.service.ManagedObjectService;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@Validated
public class ObjectController {

    private final ManagedObjectService managedObjectService;

    public ObjectController(ManagedObjectService managedObjectService) {
        this.managedObjectService = managedObjectService;
    }

    @GetMapping({"/", "/objects"})
    public String list(Model model) {
        model.addAttribute("objects", managedObjectService.findAll());
        model.addAttribute("form", new ObjectForm());
        return "objects/list";
    }

    @PostMapping("/objects")
    public String createObject(@ModelAttribute("form") ObjectForm form) {
        ManagedObject managedObject = managedObjectService.create(form.getName(), form.getDescription(), form.getPrimaryData());
        return "redirect:/objects/" + managedObject.getId();
    }

    @GetMapping("/objects/{id}")
    public String details(@PathVariable UUID id, Model model) {
        ManagedObject managedObject = managedObjectService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Object not found"));
        model.addAttribute("object", managedObject);
        return "objects/detail";
    }

    @PostMapping("/objects/{id}/files")
    public String uploadFile(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        managedObjectService.addFile(id, file);
        return "redirect:/objects/" + id;
    }

    public static class ObjectForm {
        @NotBlank
        private String name;
        private String description;
        private String primaryData;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getPrimaryData() {
            return primaryData;
        }

        public void setPrimaryData(String primaryData) {
            this.primaryData = primaryData;
        }
    }
}
