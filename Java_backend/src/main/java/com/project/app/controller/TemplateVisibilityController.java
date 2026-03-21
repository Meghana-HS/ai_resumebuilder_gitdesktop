package com.project.app.controller;

import com.project.app.service.TemplateVisibilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/template-visibility")
public class TemplateVisibilityController {

    @Autowired
    private TemplateVisibilityService templateVisibilityService;

    @GetMapping
    public ResponseEntity<Map<String, Boolean>> getVisibilityStatuses() {
        return ResponseEntity.ok(templateVisibilityService.getVisibilityStatuses());
    }

    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleVisibility(@RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(templateVisibilityService.toggleVisibility(payload.get("templateId")));
    }
}
