package com.kapamejlbka.objectmanager.domain.customer.repository;

import com.kapamejlbka.objectmanager.domain.customer.ManagedObject;
import com.kapamejlbka.objectmanager.domain.user.AppUser;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ManagedObjectRepository extends JpaRepository<ManagedObject, UUID> {

    List<ManagedObject> findAllByDeletionRequestedFalseOrderByCreatedAtDesc();

    @Query("select mo from ManagedObject mo where mo.deletionRequested = true order by mo.deletionRequestedAt desc")
    List<ManagedObject> findAllDeletionRequested();

    @Query("""
            select distinct mo from ManagedObject mo
            left join mo.owners owners
            left join mo.customer customer
            left join customer.owners customerOwners
            where mo.deletionRequested = false and (
                mo.createdBy = :user or owners = :user or customer.createdBy = :user or customerOwners = :user
            )
            order by mo.createdAt desc
            """)
    List<ManagedObject> findAllVisibleForUser(@Param("user") AppUser user);

    @Query("""
            select distinct mo from ManagedObject mo
            left join mo.owners owners
            left join mo.customer customer
            left join customer.owners customerOwners
            where mo.deletionRequested = true and (
                mo.createdBy = :user or owners = :user or customer.createdBy = :user or customerOwners = :user
            )
            order by mo.deletionRequestedAt desc
            """)
    List<ManagedObject> findAllDeletionRequestedForUser(@Param("user") AppUser user);
}
