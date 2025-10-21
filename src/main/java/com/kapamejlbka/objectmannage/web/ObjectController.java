package com.kapamejlbka.objectmannage.web;

import com.kapamejlbka.objectmannage.model.ManagedObject;
import com.kapamejlbka.objectmannage.model.ObjectChange;
import com.kapamejlbka.objectmannage.model.ProjectCustomer;
import com.kapamejlbka.objectmannage.model.StoredFile;
import com.kapamejlbka.objectmannage.model.UserAccount;
import com.kapamejlbka.objectmannage.repository.ObjectChangeRepository;
import com.kapamejlbka.objectmannage.repository.ProjectCustomerRepository;
import com.kapamejlbka.objectmannage.service.FilePreviewService;
import com.kapamejlbka.objectmannage.service.ManagedObjectService;
import com.kapamejlbka.objectmannage.service.UserService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/objects")
@Validated
public class ObjectController {

    private final ManagedObjectService managedObjectService;
    private final ProjectCustomerRepository customerRepository;
    private final ObjectChangeRepository objectChangeRepository;
    private final UserService userService;
    private final FilePreviewService filePreviewService;

    public ObjectController(ManagedObjectService managedObjectService,
                            ProjectCustomerRepository customerRepository,
                            ObjectChangeRepository objectChangeRepository,
                            UserService userService,
                            FilePreviewService filePreviewService) {
        this.managedObjectService = managedObjectService;
        this.customerRepository = customerRepository;
        this.objectChangeRepository = objectChangeRepository;
        this.userService = userService;
        this.filePreviewService = filePreviewService;
    }

    @ModelAttribute("customers")
    public List<ProjectCustomer> customers() {
        return customerRepository.findAll();
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("objects", managedObjectService.listVisibleObjects());
        return "objects/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("form", new ObjectForm());
        return "objects/create";
    }

    @PostMapping
    public String createObject(@Validated @ModelAttribute("form") ObjectForm form, Principal principal) {
        UserAccount user = userService.findByUsername(principal.getName());
        ManagedObject managedObject = managedObjectService.create(
                form.getName(),
                form.getDescription(),
                form.getPrimaryData(),
                form.getCustomerId(),
                user
        );
        return "redirect:/objects/" + managedObject.getId();
    }

    @GetMapping("/{id}")
    public String details(@PathVariable UUID id, Model model) {
        ManagedObject managedObject = managedObjectService.getById(id);
        List<ObjectChange> history = objectChangeRepository.findAllByManagedObjectOrderByChangedAtDesc(managedObject);
        model.addAttribute("object", managedObject);
        model.addAttribute("history", history);
        return "objects/detail";
    }

    @PostMapping("/{id}/files")
    public String uploadFile(@PathVariable UUID id, @RequestParam("file") MultipartFile[] files, Principal principal) {
        UserAccount user = userService.findByUsername(principal.getName());
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                managedObjectService.addFile(id, file, user);
            }
        }
        return "redirect:/objects/" + id;
    }

    @GetMapping("/{objectId}/files/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID objectId,
                                                 @PathVariable UUID fileId,
                                                 @RequestParam(value = "download", defaultValue = "false") boolean download) {
        StoredFile file = managedObjectService.getFile(objectId, fileId);
        Resource resource = managedObjectService.loadFileResource(file);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (StringUtils.hasText(file.getContentType())) {
            try {
                mediaType = MediaType.parseMediaType(file.getContentType());
            } catch (IllegalArgumentException ignored) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
        }

        ContentDisposition disposition = (download ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(file.getOriginalFilename(), StandardCharsets.UTF_8)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDisposition(disposition);
        if (file.getSize() > 0) {
            headers.setContentLength(file.getSize());
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @GetMapping("/{objectId}/files/{fileId}/preview")
    public ResponseEntity<String> previewFile(@PathVariable UUID objectId,
                                              @PathVariable UUID fileId) {
        StoredFile file = managedObjectService.getFile(objectId, fileId);
        String previewHtml = filePreviewService.renderPreview(file);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(previewHtml);
    }

    @PostMapping("/{id}/request-delete")
    public String requestDelete(@PathVariable UUID id, Principal principal) {
        UserAccount user = userService.findByUsername(principal.getName());
        managedObjectService.requestDeletion(id, user);
        return "redirect:/objects";
    }

    public static class ObjectForm {
        @NotBlank
        private String name;
        private String description;
        private String primaryData;
        @NotNull
        private UUID customerId;

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

        public UUID getCustomerId() {
            return customerId;
        }

        public void setCustomerId(UUID customerId) {
            this.customerId = customerId;
        }
    }
}
