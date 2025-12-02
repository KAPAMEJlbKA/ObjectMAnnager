package com.kapamejlbka.objectmanager.domain.customer.repository;

import com.kapamejlbka.objectmanager.domain.customer.ManagedObject;
import com.kapamejlbka.objectmanager.domain.customer.ObjectChange;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObjectChangeRepository extends JpaRepository<ObjectChange, UUID> {

    List<ObjectChange> findAllByManagedObjectOrderByChangedAtDesc(ManagedObject object);
}
