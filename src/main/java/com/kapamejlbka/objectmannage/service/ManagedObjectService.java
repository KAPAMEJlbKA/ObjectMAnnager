package com.kapamejlbka.objectmannage.service;

import com.kapamejlbka.objectmannage.model.ManagedObject;
import com.kapamejlbka.objectmannage.model.ObjectChange;
import com.kapamejlbka.objectmannage.model.ObjectChangeType;
import com.kapamejlbka.objectmannage.model.ProjectCustomer;
import com.kapamejlbka.objectmannage.model.StoredFile;
import com.kapamejlbka.objectmannage.model.UserAccount;
import com.kapamejlbka.objectmannage.repository.ManagedObjectRepository;
import com.kapamejlbka.objectmannage.repository.ObjectChangeRepository;
import com.kapamejlbka.objectmannage.repository.ProjectCustomerRepository;
import com.kapamejlbka.objectmannage.repository.StoredFileRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ManagedObjectService {

    private final ManagedObjectRepository managedObjectRepository;
    private final ProjectCustomerRepository customerRepository;
    private final ObjectChangeRepository objectChangeRepository;
    private final FileStorageService storageService;
    private final StoredFileRepository storedFileRepository;

    public ManagedObjectService(
            ManagedObjectRepository managedObjectRepository,
            ProjectCustomerRepository customerRepository,
            ObjectChangeRepository objectChangeRepository,
            FileStorageService storageService,
            StoredFileRepository storedFileRepository) {
        this.managedObjectRepository = managedObjectRepository;
        this.customerRepository = customerRepository;
        this.objectChangeRepository = objectChangeRepository;
        this.storageService = storageService;
        this.storedFileRepository = storedFileRepository;
    }

    public List<ManagedObject> listVisibleObjects() {
        return managedObjectRepository.findAllByDeletionRequestedFalseOrderByCreatedAtDesc();
    }

    public List<ManagedObject> listPendingDeletion() {
        return managedObjectRepository.findAllDeletionRequested();
    }

    public ManagedObject getById(UUID id) {
        return managedObjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Object not found: " + id));
    }

    @Transactional
    public ManagedObject create(String name, String description,
                               UUID customerId, Double latitude, Double longitude,
                               UserAccount creator) {
        ProjectCustomer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
        ManagedObject managedObject = new ManagedObject(name, description, null, customer, latitude, longitude);
        managedObject.setCreatedAt(LocalDateTime.now());
        managedObject.setUpdatedAt(LocalDateTime.now());
        ManagedObject saved = managedObjectRepository.save(managedObject);
        ObjectChange change = new ObjectChange(ObjectChangeType.CREATED, null, null, null,
                "Создан объект пользователем " + creator.getUsername());
        change.setUser(creator);
        change.setManagedObject(saved);
        objectChangeRepository.save(change);
        return saved;
    }

    @Transactional
    public ManagedObject update(UUID id, String name, String description, String primaryData,
                               UUID customerId, Double latitude, Double longitude,
                               UserAccount editor) {
        ManagedObject managedObject = getById(id);
        ProjectCustomer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
        if (!Objects.equals(managedObject.getName(), name)) {
            recordChange(managedObject, editor, ObjectChangeType.UPDATED, "name", managedObject.getName(), name);
            managedObject.setName(name);
        }
        if (!Objects.equals(managedObject.getDescription(), description)) {
            recordChange(managedObject, editor, ObjectChangeType.UPDATED, "description", managedObject.getDescription(), description);
            managedObject.setDescription(description);
        }
        if (!Objects.equals(managedObject.getPrimaryData(), primaryData)) {
            recordChange(managedObject, editor, ObjectChangeType.UPDATED, "primaryData", managedObject.getPrimaryData(), primaryData);
            managedObject.setPrimaryData(primaryData);
        }
        if (!managedObject.getCustomer().getId().equals(customerId)) {
            recordChange(managedObject, editor, ObjectChangeType.TRANSFERRED,
                    "customer", managedObject.getCustomer().getName(), customer.getName());
            managedObject.setCustomer(customer);
        }
        if (!Objects.equals(managedObject.getLatitude(), latitude)) {
            recordChange(managedObject, editor, ObjectChangeType.UPDATED,
                    "latitude",
                    managedObject.getLatitude() == null ? null : managedObject.getLatitude().toString(),
                    latitude == null ? null : latitude.toString());
            managedObject.setLatitude(latitude);
        }
        if (!Objects.equals(managedObject.getLongitude(), longitude)) {
            recordChange(managedObject, editor, ObjectChangeType.UPDATED,
                    "longitude",
                    managedObject.getLongitude() == null ? null : managedObject.getLongitude().toString(),
                    longitude == null ? null : longitude.toString());
            managedObject.setLongitude(longitude);
        }
        managedObject.setUpdatedAt(LocalDateTime.now());
        return managedObjectRepository.save(managedObject);
    }

    @Transactional
    public ManagedObject updatePrimaryData(UUID id, String primaryData, UserAccount editor) {
        ManagedObject managedObject = getById(id);
        if (!Objects.equals(managedObject.getPrimaryData(), primaryData)) {
            recordChange(managedObject, editor, ObjectChangeType.UPDATED,
                    "primaryData", managedObject.getPrimaryData(), primaryData);
            managedObject.setPrimaryData(primaryData);
            managedObject.setUpdatedAt(LocalDateTime.now());
            managedObjectRepository.save(managedObject);
        }
        return managedObject;
    }

    @Transactional
    public StoredFile addFile(UUID objectId, MultipartFile file, UserAccount uploader) {
        ManagedObject managedObject = getById(objectId);
        StoredFile storedFile = storageService.store(managedObject, file);
        managedObject.addFile(storedFile);
        managedObject.setUpdatedAt(LocalDateTime.now());
        managedObjectRepository.save(managedObject);
        recordChange(managedObject, uploader, ObjectChangeType.FILE_ATTACHED,
                "file", null, storedFile.getOriginalFilename());
        return storedFile;
    }

    public StoredFile getFile(UUID objectId, UUID fileId) {
        return storedFileRepository.findByIdAndManagedObjectId(fileId, objectId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Файл " + fileId + " не найден для объекта " + objectId));
    }

    public Resource loadFileResource(StoredFile storedFile) {
        return storageService.loadAsResource(storedFile);
    }

    @Transactional
    public void requestDeletion(UUID objectId, UserAccount requester) {
        ManagedObject managedObject = getById(objectId);
        managedObject.setDeletionRequested(true);
        managedObject.setDeletionRequestedAt(LocalDateTime.now());
        managedObject.setDeletionRequestedBy(requester);
        managedObject.setUpdatedAt(LocalDateTime.now());
        managedObjectRepository.save(managedObject);
        recordChange(managedObject, requester, ObjectChangeType.DELETION_REQUESTED, null, null, null);
    }

    @Transactional
    public void revokeDeletion(UUID objectId, UserAccount requester) {
        ManagedObject managedObject = getById(objectId);
        managedObject.setDeletionRequested(false);
        managedObject.setDeletionRequestedAt(null);
        managedObject.setDeletionRequestedBy(null);
        managedObjectRepository.save(managedObject);
        recordChange(managedObject, requester, ObjectChangeType.DELETION_REVOKED, null, null, null);
    }

    @Transactional
    public void deletePermanently(UUID objectId, UserAccount admin) {
        ManagedObject managedObject = getById(objectId);
        managedObject.getFiles().forEach(storageService::deleteFile);
        recordChange(managedObject, admin, ObjectChangeType.DELETED, null, null,
                "Объект \"" + managedObject.getName() + "\" удалён администратором " + admin.getUsername());
        managedObjectRepository.delete(managedObject);
    }

    private void recordChange(ManagedObject managedObject, UserAccount user, ObjectChangeType type,
                              String field, String oldValue, String newValue) {
        String summary;
        if (type == ObjectChangeType.FILE_ATTACHED) {
            summary = "Добавлен файл " + newValue;
        } else if (type == ObjectChangeType.TRANSFERRED) {
            summary = "Объект перенесён от " + oldValue + " к " + newValue;
        } else if (type == ObjectChangeType.DELETION_REQUESTED) {
            summary = "Пользователь запросил удаление";
        } else if (type == ObjectChangeType.DELETION_REVOKED) {
            summary = "Запрос на удаление отозван";
        } else if (type == ObjectChangeType.DELETED) {
            summary = newValue;
        } else {
            summary = buildChangeSummary(user, field);
        }
        String effectiveField = field;
        String effectiveOldValue = oldValue;
        String effectiveNewValue = newValue;

        if (type == ObjectChangeType.DELETED) {
            effectiveField = effectiveField == null ? "objectId" : effectiveField;
            effectiveOldValue = managedObject.getId() != null ? managedObject.getId().toString() : null;
            effectiveNewValue = managedObject.getName();
        } else if ("primaryData".equals(field)) {
            effectiveOldValue = null;
            effectiveNewValue = null;
        }

        ObjectChange change = new ObjectChange(type, effectiveField, effectiveOldValue, effectiveNewValue, summary);
        if (type != ObjectChangeType.DELETED) {
            change.setManagedObject(managedObject);
        }
        change.setUser(user);
        objectChangeRepository.save(change);
    }

    private String buildChangeSummary(UserAccount user, String field) {
        String username = user != null ? user.getUsername() : "Система";
        String section = describeSection(field);
        return username + " изменил " + section;
    }

    private String describeSection(String field) {
        if (!StringUtils.hasText(field)) {
            return "данные объекта";
        }
        return switch (field) {
            case "name" -> "название объекта";
            case "description" -> "описание объекта";
            case "primaryData" -> "раздел \"Первичные данные\"";
            case "customer" -> "раздел \"Заказчик\"";
            case "latitude", "longitude" -> "координаты объекта";
            default -> "поле \"" + field + "\"";
        };
    }
}
