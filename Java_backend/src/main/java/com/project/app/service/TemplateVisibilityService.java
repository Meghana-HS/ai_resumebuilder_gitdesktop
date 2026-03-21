package com.project.app.service;

import com.project.app.entity.TemplateVisibility;
import com.project.app.repository.TemplateVisibilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TemplateVisibilityService {

    @Autowired
    private TemplateVisibilityRepository templateVisibilityRepository;

    public Map<String, Boolean> getVisibilityStatuses() {
        Map<String, Boolean> response = new LinkedHashMap<>();
        templateVisibilityRepository.findAll().forEach(item -> response.put(item.getTemplateId(), item.getIsActive()));
        return response;
    }

    public Map<String, Object> toggleVisibility(String templateId) {
        if (templateId == null || templateId.isBlank()) {
            throw new IllegalArgumentException("Template ID is required");
        }

        TemplateVisibility visibility = templateVisibilityRepository.findByTemplateId(templateId).orElse(null);
        if (visibility == null) {
            visibility = new TemplateVisibility();
            visibility.setTemplateId(templateId);
            visibility.setIsActive(false);
        } else {
            visibility.setIsActive(!Boolean.TRUE.equals(visibility.getIsActive()));
        }

        TemplateVisibility saved = templateVisibilityRepository.save(visibility);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("msg", "Visibility updated");
        response.put("templateId", saved.getTemplateId());
        response.put("isActive", saved.getIsActive());
        return response;
    }
}
