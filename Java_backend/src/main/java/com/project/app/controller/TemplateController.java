package com.project.app.controller;

import com.project.app.dto.ApiResponse;
import com.project.app.entity.Template;
import com.project.app.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/template")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Template>>> getTemplates() {
        List<Template> templates = templateService.getAllTemplates();
        return ResponseEntity.ok(ApiResponse.success("Templates retrieved", templates));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Template>> getTemplateById(@PathVariable Long id) {
        Optional<Template> template = templateService.getTemplateById(id);
        if (template.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success("Template retrieved", template.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/parse/{id}")
    public ResponseEntity<String> getTemplateHtml(@PathVariable Long id) {
        try {
            String html = templateService.getTemplateHtml(id);
            return ResponseEntity.ok(html);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Template>> uploadTemplate(@ModelAttribute Template template) {
        try {
            Template createdTemplate = templateService.createTemplate(template);
            return ResponseEntity.status(201).body(ApiResponse.success("Template uploaded successfully", createdTemplate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Template upload failed"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Template>> updateTemplate(@PathVariable Long id, @ModelAttribute Template template) {
        try {
            Template updatedTemplate = templateService.updateTemplate(id, template);
            return ResponseEntity.ok(ApiResponse.success("Template updated", updatedTemplate));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<ApiResponse<Template>> approveTemplate(@PathVariable Long id) {
        try {
            Template approvedTemplate = templateService.approveTemplate(id);
            return ResponseEntity.ok(ApiResponse.success("Template approved", approvedTemplate));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTemplate(@PathVariable Long id) {
        try {
            templateService.deleteTemplate(id);
            return ResponseEntity.ok(ApiResponse.success("Template deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
