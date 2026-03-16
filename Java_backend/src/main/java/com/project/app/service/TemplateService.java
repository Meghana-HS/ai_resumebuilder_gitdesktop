package com.project.app.service;

import com.project.app.entity.Template;
import com.project.app.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TemplateService {

    @Autowired
    private TemplateRepository templateRepository;

    public List<Template> getAllTemplates() {
        return templateRepository.findByStatus(Template.TemplateStatus.APPROVED);
    }

    public Optional<Template> getTemplateById(Long id) {
        return templateRepository.findById(id);
    }

    public Template createTemplate(Template template) {
        template.setStatus(Template.TemplateStatus.PENDING);
        return templateRepository.save(template);
    }

    public Template updateTemplate(Long id, Template templateDetails) {
        Optional<Template> templateOpt = templateRepository.findById(id);
        if (templateOpt.isEmpty()) {
            throw new RuntimeException("Template not found");
        }

        Template template = templateOpt.get();
        template.setName(templateDetails.getName());
        template.setDescription(templateDetails.getDescription());
        template.setPreviewimage(templateDetails.getPreviewimage());
        template.setFilePath(templateDetails.getFilePath());
        template.setCategory(templateDetails.getCategory());

        return templateRepository.save(template);
    }

    public Template approveTemplate(Long id) {
        Optional<Template> templateOpt = templateRepository.findById(id);
        if (templateOpt.isEmpty()) {
            throw new RuntimeException("Template not found");
        }

        Template template = templateOpt.get();
        template.setStatus(Template.TemplateStatus.APPROVED);
        return templateRepository.save(template);
    }

    public void deleteTemplate(Long id) {
        if (!templateRepository.existsById(id)) {
            throw new RuntimeException("Template not found");
        }
        templateRepository.deleteById(id);
    }

    public String getTemplateHtml(Long id) {
        Optional<Template> templateOpt = templateRepository.findById(id);
        if (templateOpt.isEmpty()) {
            throw new RuntimeException("Template not found");
        }

        // TODO: Implement HTML parsing logic for template
        // This would read the template file and return HTML content
        return "<html><body>Template HTML content</body></html>";
    }
}
