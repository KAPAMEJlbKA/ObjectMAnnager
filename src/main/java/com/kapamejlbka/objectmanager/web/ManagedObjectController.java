package com.kapamejlbka.objectmanager.web;

import com.kapamejlbka.objectmanager.domain.customer.ManagedObject;
import com.kapamejlbka.objectmanager.domain.customer.ObjectChange;
import com.kapamejlbka.objectmanager.domain.user.AppUser;
import com.kapamejlbka.objectmanager.service.AccessControlService;
import com.kapamejlbka.objectmanager.service.ManagedObjectService;
import com.kapamejlbka.objectmanager.service.UserService;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ManagedObjectController {

    private final ManagedObjectService managedObjectService;
    private final UserService userService;
    private final AccessControlService accessControlService;

    public ManagedObjectController(ManagedObjectService managedObjectService, UserService userService,
                                   AccessControlService accessControlService) {
        this.managedObjectService = managedObjectService;
        this.userService = userService;
        this.accessControlService = accessControlService;
    }

    @GetMapping("/objects/{id}")
    public String detail(@PathVariable("id") UUID id, Model model) {
        ManagedObject managedObject = managedObjectService.getById(id);
        List<ObjectChange> changes = managedObjectService.getChangeHistory(id);
        AppUser currentUser = getCurrentUser();
        accessControlService.ensureCanViewObject(managedObject, currentUser);
        boolean isAuthor = isAuthor(currentUser, managedObject);
        model.addAttribute("object", managedObject);
        model.addAttribute("changes", changes);
        model.addAttribute("isAuthor", isAuthor);
        model.addAttribute("canRequestDeletion", isAuthor || isAdmin(currentUser));
        model.addAttribute("canRevokeDeletion", canRevoke(managedObject, currentUser, isAuthor));
        model.addAttribute("canDeletePermanently", isAdmin(currentUser));
        return "objects/detail";
    }

    @PostMapping("/objects/{id}/editor/start")
    public String startEditing(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        AppUser currentUser = getCurrentUser();
        try {
            managedObjectService.startEditing(id, currentUser);
            redirectAttributes.addFlashAttribute("flashSuccess", "Редактор открыт, объект заблокирован для других пользователей");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        return "redirect:/objects/" + id;
    }

    @PostMapping("/objects/{id}/calculation/complete")
    public String completeCalculation(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        AppUser currentUser = getCurrentUser();
        managedObjectService.markCalculated(id, currentUser);
        redirectAttributes.addFlashAttribute("flashSuccess", "Статус объекта обновлён на CALCULATED");
        return "redirect:/objects/" + id;
    }

    @PostMapping("/objects/{id}/calculation/reset")
    public String resetCalculation(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        AppUser currentUser = getCurrentUser();
        managedObjectService.resetStatus(id, currentUser);
        redirectAttributes.addFlashAttribute("flashSuccess", "Статус объекта сброшен на NOT_CALCULATED");
        return "redirect:/objects/" + id;
    }

    @PostMapping("/objects/{id}/deletion/request")
    public String requestDeletion(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        ManagedObject managedObject = managedObjectService.getById(id);
        AppUser currentUser = getCurrentUser();
        accessControlService.ensureCanEditObject(managedObject, currentUser);
        boolean isAuthor = isAuthor(currentUser, managedObject);
        if (!isAuthor && !isAdmin(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Только автор объекта или администратор могут запросить удаление");
        }
        if (managedObject.isDeletionRequested()) {
            redirectAttributes.addFlashAttribute("flashError", "Запрос на удаление уже активен");
        } else {
            managedObjectService.requestDeletion(id, currentUser);
            redirectAttributes.addFlashAttribute("flashSuccess", "Запрос на удаление отправлен");
        }
        return "redirect:/objects/" + id;
    }

    @PostMapping("/objects/{id}/deletion/revoke")
    public String revokeDeletion(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        ManagedObject managedObject = managedObjectService.getById(id);
        AppUser currentUser = getCurrentUser();
        accessControlService.ensureCanEditObject(managedObject, currentUser);
        boolean isAuthor = isAuthor(currentUser, managedObject);
        boolean canRevoke = canRevoke(managedObject, currentUser, isAuthor);
        if (!canRevoke) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Отозвать запрос может только автор, инициатор или администратор");
        }
        if (!managedObject.isDeletionRequested()) {
            redirectAttributes.addFlashAttribute("flashError", "Нет активного запроса на удаление");
        } else {
            managedObjectService.revokeDeletion(id, currentUser);
            redirectAttributes.addFlashAttribute("flashSuccess", "Запрос на удаление отозван");
        }
        return "redirect:/objects/" + id;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/objects/{id}/delete")
    public String deletePermanently(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        AppUser currentUser = getCurrentUser();
        managedObjectService.deletePermanently(id, currentUser);
        redirectAttributes.addFlashAttribute("flashSuccess", "Объект удалён навсегда");
        return "redirect:/";
    }

    private boolean canRevoke(ManagedObject managedObject, AppUser currentUser, boolean isAuthor) {
        if (isAdmin(currentUser)) {
            return true;
        }
        if (isAuthor) {
            return true;
        }
        AppUser requestedBy = managedObject.getDeletionRequestedBy();
        return requestedBy != null && Objects.equals(requestedBy.getId(), currentUser.getId());
    }

    private boolean isAuthor(AppUser currentUser, ManagedObject object) {
        if (currentUser == null || object.getCreatedBy() == null) {
            return false;
        }
        return Objects.equals(object.getCreatedBy().getId(), currentUser.getId());
    }

    private boolean isAdmin(AppUser user) {
        return accessControlService.isAdmin(user);
    }

    private AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        return userService.getByUsername(authentication.getName());
    }
}

