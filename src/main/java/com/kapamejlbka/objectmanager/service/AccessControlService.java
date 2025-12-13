package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.customer.ManagedObject;
import com.kapamejlbka.objectmanager.domain.customer.ProjectCustomer;
import com.kapamejlbka.objectmanager.domain.user.AppRole;
import com.kapamejlbka.objectmanager.domain.user.AppUser;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccessControlService {

    public boolean isAdmin(AppUser user) {
        if (user == null) {
            return false;
        }
        return user.getRoles().stream()
                .map(AppRole::getName)
                .anyMatch(role -> "ADMIN".equalsIgnoreCase(role));
    }

    public boolean canAccessCustomer(ProjectCustomer customer, AppUser user) {
        if (customer == null || user == null) {
            return false;
        }
        return isAdmin(user)
                || Objects.equals(
                        customer.getCreatedBy() == null ? null : customer.getCreatedBy().getId(),
                        user.getId())
                || customer.getOwners().stream().anyMatch(owner -> Objects.equals(owner.getId(), user.getId()));
    }

    public boolean canAccessObject(ManagedObject managedObject, AppUser user) {
        if (managedObject == null || user == null) {
            return false;
        }
        return isAdmin(user)
                || Objects.equals(
                        managedObject.getCreatedBy() == null ? null : managedObject.getCreatedBy().getId(),
                        user.getId())
                || managedObject.getOwners().stream().anyMatch(owner -> Objects.equals(owner.getId(), user.getId()))
                || canAccessCustomer(managedObject.getCustomer(), user);
    }

    public void ensureCanViewObject(ManagedObject managedObject, AppUser user) {
        if (!canAccessObject(managedObject, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа к объекту");
        }
    }

    public void ensureCanEditObject(ManagedObject managedObject, AppUser user) {
        if (!canAccessObject(managedObject, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа к изменению объекта");
        }
    }

    public void ensureCanViewCustomer(ProjectCustomer customer, AppUser user) {
        if (!canAccessCustomer(customer, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа к заказчику");
        }
    }
}
