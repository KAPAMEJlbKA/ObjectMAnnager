package com.kapamejlbka.objectmannage.repository;

import com.kapamejlbka.objectmannage.model.ManagedObject;
import com.kapamejlbka.objectmannage.model.ObjectChange;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObjectChangeRepository extends JpaRepository<ObjectChange, UUID> {

    List<ObjectChange> findAllByManagedObjectOrderByChangedAtDesc(ManagedObject object);
}
