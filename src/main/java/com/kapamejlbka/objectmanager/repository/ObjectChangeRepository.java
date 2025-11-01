package com.kapamejlbka.objectmanager.repository;

import com.kapamejlbka.objectmanager.model.ManagedObject;
import com.kapamejlbka.objectmanager.model.ObjectChange;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObjectChangeRepository extends JpaRepository<ObjectChange, UUID> {

    List<ObjectChange> findAllByManagedObjectOrderByChangedAtDesc(ManagedObject object);
}
