package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.material.Material;
import com.kapamejlbka.objectmanager.domain.material.dto.MaterialForm;
import com.kapamejlbka.objectmanager.repository.MaterialRepository;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;

    public MaterialService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    public List<Material> listAll() {
        return materialRepository.findAll();
    }

    public List<String> listCategories() {
        Set<String> categories = new HashSet<>();
        for (Material material : materialRepository.findAll()) {
            if (StringUtils.hasText(material.getCategory())) {
                categories.add(material.getCategory());
            }
        }
        return categories.stream().sorted(String::compareToIgnoreCase).collect(Collectors.toList());
    }

    public Material getById(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Материал не найден"));
    }

    public List<Material> search(String query, String category) {
        List<Material> all = materialRepository.findAll();
        return all.stream()
                .filter(material -> {
                    if (StringUtils.hasText(category)) {
                        return category.equalsIgnoreCase(material.getCategory());
                    }
                    return true;
                })
                .filter(material -> {
                    if (!StringUtils.hasText(query)) {
                        return true;
                    }
                    String normalized = query.trim().toLowerCase(Locale.ROOT);
                    return material.getCode().toLowerCase(Locale.ROOT).contains(normalized)
                            || material.getName().toLowerCase(Locale.ROOT).contains(normalized);
                })
                .sorted((a, b) -> a.getCode().compareToIgnoreCase(b.getCode()))
                .collect(Collectors.toList());
    }

    @Transactional
    public Material create(MaterialForm form) {
        validateCode(form.getCode(), null);
        validateName(form.getName());
        Material material = new Material();
        material.setCode(form.getCode().trim().toUpperCase(Locale.ROOT));
        material.setName(form.getName().trim());
        material.setCategory(normalizeRequired(form.getCategory(), "Категория обязательна"));
        material.setUnit(normalizeRequired(form.getUnit(), "Единица измерения обязательна"));
        material.setNotes(form.getNotes());
        return materialRepository.save(material);
    }

    @Transactional
    public Material update(Long id, MaterialForm form) {
        Material existing = getById(id);
        validateCode(form.getCode(), id);
        validateName(form.getName());
        existing.setCode(form.getCode().trim().toUpperCase(Locale.ROOT));
        existing.setName(form.getName().trim());
        existing.setCategory(normalizeRequired(form.getCategory(), "Категория обязательна"));
        existing.setUnit(normalizeRequired(form.getUnit(), "Единица измерения обязательна"));
        existing.setNotes(form.getNotes());
        return materialRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        materialRepository.deleteById(id);
    }

    private void validateCode(String code, Long excludeId) {
        if (!StringUtils.hasText(code)) {
            throw new IllegalArgumentException("Код материала обязателен");
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        materialRepository.findByCode(normalized)
                .filter(material -> excludeId == null || !excludeId.equals(material.getId()))
                .ifPresent(material -> { throw new IllegalArgumentException("Материал с таким кодом уже существует"); });
    }

    private void validateName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Название обязательно");
        }
    }

    private String normalizeRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
