package com.kapamejlbka.objectmannage.service;

import com.kapamejlbka.objectmannage.model.ManagedObject;
import com.kapamejlbka.objectmannage.model.StoredFile;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ManagedObjectService {

    private final Map<UUID, ManagedObject> objects = new ConcurrentHashMap<>();
    private final FileStorageService storageService;

    public ManagedObjectService(FileStorageService storageService) {
        this.storageService = storageService;
    }

    public Collection<ManagedObject> findAll() {
        return objects.values();
    }

    public Optional<ManagedObject> findById(UUID id) {
        return Optional.ofNullable(objects.get(id));
    }

    public ManagedObject create(String name, String description, String primaryData) {
        UUID id = UUID.randomUUID();
        ManagedObject managedObject = new ManagedObject(id, name, description, primaryData);
        objects.put(id, managedObject);
        return managedObject;
    }

    public StoredFile addFile(UUID objectId, MultipartFile file) {
        ManagedObject managedObject = objects.get(objectId);
        if (managedObject == null) {
            throw new IllegalArgumentException("Object not found: " + objectId);
        }
        StoredFile storedFile = storageService.store(objectId, file);
        managedObject.addFile(storedFile);
        return storedFile;
    }
}
