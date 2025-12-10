package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.material.Material;
import com.kapamejlbka.objectmanager.domain.material.MaterialNorm;
import com.kapamejlbka.objectmanager.domain.material.MaterialNormContext;
import com.kapamejlbka.objectmanager.domain.material.dto.MaterialNormForm;
import com.kapamejlbka.objectmanager.repository.MaterialNormRepository;
import com.kapamejlbka.objectmanager.repository.MaterialRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MaterialNormService {

    private final MaterialNormRepository normRepository;
    private final MaterialRepository materialRepository;

    public MaterialNormService(MaterialNormRepository normRepository, MaterialRepository materialRepository) {
        this.normRepository = normRepository;
        this.materialRepository = materialRepository;
    }

    public List<MaterialNorm> listAll() {
        return normRepository.findAll();
    }

    public MaterialNorm getById(Long id) {
        return normRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Норма не найдена"));
    }

    @Transactional
    public MaterialNorm create(MaterialNormForm form) {
        validate(form);
        MaterialNorm norm = new MaterialNorm();
        norm.setContextType(form.getContextType());
        norm.setFormula(form.getFormula().trim());
        norm.setDescription(form.getDescription());
        norm.setMaterial(resolveMaterial(form.getMaterialId()));
        return normRepository.save(norm);
    }

    @Transactional
    public MaterialNorm update(Long id, MaterialNormForm form) {
        validate(form);
        MaterialNorm existing = getById(id);
        existing.setContextType(form.getContextType());
        existing.setFormula(form.getFormula().trim());
        existing.setDescription(form.getDescription());
        existing.setMaterial(resolveMaterial(form.getMaterialId()));
        return normRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        normRepository.deleteById(id);
    }

    public String resolveContextName(MaterialNormContext contextType) {
        if (contextType == null) {
            return "";
        }
        return contextType.getDisplayNameRu();
    }

    public List<MaterialNormContext> availableContextCodes() {
        return MaterialNormContext.orderedValues();
    }

    public Material resolveMaterial(Long materialId) {
        if (materialId == null) {
            throw new IllegalArgumentException("Укажите материал");
        }
        return materialRepository.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Материал не найден"));
    }

    private void validate(MaterialNormForm form) {
        if (form.getContextType() == null) {
            throw new IllegalArgumentException("Контекст обязателен");
        }
        if (!StringUtils.hasText(form.getFormula())) {
            throw new IllegalArgumentException("Формула обязательна");
        }
    }
}
