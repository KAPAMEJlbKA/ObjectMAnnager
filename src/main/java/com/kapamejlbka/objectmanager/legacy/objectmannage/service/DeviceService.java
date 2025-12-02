package com.kapamejlbka.objectmanager.legacy.objectmannage.service;

import com.kapamejlbka.objectmanager.legacy.objectmannage.domain.Device;
import com.kapamejlbka.objectmanager.legacy.objectmannage.domain.DeviceType;
import com.kapamejlbka.objectmanager.legacy.objectmannage.domain.Site;
import com.kapamejlbka.objectmanager.legacy.objectmannage.repository.DeviceRepository;
import com.kapamejlbka.objectmanager.legacy.objectmannage.repository.SiteRepository;
import com.kapamejlbka.objectmanager.legacy.objectmannage.web.form.DeviceForm;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final SiteRepository siteRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public DeviceService(DeviceRepository deviceRepository, SiteRepository siteRepository) {
        this.deviceRepository = deviceRepository;
        this.siteRepository = siteRepository;
    }

    public Device create(DeviceForm form) {
        Site site = siteRepository.findById(form.getSiteId())
                .orElseThrow(() -> new EntityNotFoundException("Site not found: " + form.getSiteId()));
        Device device = new Device();
        form.applyTo(device, site);
        return deviceRepository.save(device);
    }

    public Device update(UUID id, DeviceForm form) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Device not found: " + id));
        Site site = siteRepository.findById(form.getSiteId())
                .orElseThrow(() -> new EntityNotFoundException("Site not found: " + form.getSiteId()));
        form.applyTo(device, site);
        return deviceRepository.save(device);
    }

    @Transactional(readOnly = true)
    public Page<Device> findPage(UUID siteId, DeviceType type, String query, Pageable pageable) {
        Specification<Device> specification = Specification.where((Specification<Device>) null);

        if (siteId != null) {
            specification = specification.and((root, cq, cb) -> cb.equal(root.get("site").get("id"), siteId));
        }

        if (type != null) {
            specification = specification.and((root, cq, cb) -> cb.equal(root.get("type"), type));
        }

        if (query != null && !query.isBlank()) {
            String normalized = "%" + query.trim().toLowerCase() + "%";
            specification = specification.and((root, cq, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.like(cb.lower(cb.coalesce(root.<String>get("model"), "")), normalized));
                predicates.add(cb.like(cb.lower(cb.coalesce(root.<String>get("serial"), "")), normalized));
                predicates.add(cb.like(cb.lower(cb.coalesce(root.<String>get("ip"), "")), normalized));
                return cb.or(predicates.toArray(new Predicate[0]));
            });
        }

        return deviceRepository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    public Device get(UUID id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Device not found: " + id));
    }

    public void delete(UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Device not found: " + id));

        if (hasInProgressWorkOrders(id)) {
            throw new IllegalStateException("Для устройства существуют незавершенные наряды");
        }

        deviceRepository.delete(device);
    }

    private boolean hasInProgressWorkOrders(UUID deviceId) {
        String sql = "select 1 from work_order where device_id = :deviceId and status = :status limit 1";
        List<?> result = entityManager.createNativeQuery(sql)
                .setParameter("deviceId", deviceId)
                .setParameter("status", "IN_PROGRESS")
                .setMaxResults(1)
                .getResultList();
        return !result.isEmpty();
    }
}
