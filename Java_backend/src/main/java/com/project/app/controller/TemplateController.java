package com.project.app.controller;

import com.project.app.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/template")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            return Long.parseLong(authentication.getName());
        } catch (Exception ignored) {
            return null;
        }
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getTemplates(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(templateService.getTemplates(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(templateService.getTemplateById(id));
    }

    @GetMapping("/parse/{id}")
    public ResponseEntity<Map<String, Object>> getTemplateHtml(@PathVariable Long id) {
        return ResponseEntity.ok(templateService.getTemplateHtml(id));
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadTemplate(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) MultipartFile templateFile,
        @RequestParam(required = false) MultipartFile thumbnail
    ) {
        return ResponseEntity.status(201).body(templateService.uploadTemplate(name, category, templateFile, thumbnail, getCurrentUserId()));
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<Map<String, Object>> approveTemplate(@PathVariable Long id) {
        return ResponseEntity.ok(templateService.approveTemplate(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTemplate(
        @PathVariable Long id,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) MultipartFile templateFile,
        @RequestParam(required = false) MultipartFile thumbnail
    ) {
        return ResponseEntity.ok(templateService.updateTemplate(id, name, category, templateFile, thumbnail, getCurrentUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTemplate(@PathVariable Long id) {
        return ResponseEntity.ok(templateService.deleteTemplate(id, getCurrentUserId()));
    }
}
