package com.kapamejlbka.objectmanager.repository;

import com.kapamejlbka.objectmanager.model.ManagedObject;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ManagedObjectRepository extends JpaRepository<ManagedObject, UUID> {

    List<ManagedObject> findAllByDeletionRequestedFalseOrderByCreatedAtDesc();

    @Query("select mo from ManagedObject mo where mo.deletionRequested = true order by mo.deletionRequestedAt desc")
    List<ManagedObject> findAllDeletionRequested();
}
