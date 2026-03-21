package com.project.app.controller;

import com.project.app.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/dashboard-stat")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            return ResponseEntity.ok(analyticsService.getAdminDashboardStats());
        } catch (Exception exception) {
            return ResponseEntity.status(500).body(Map.of("message", "Dashboard stats fetch failed", "error", exception.getMessage()));
        }
    }

    @GetMapping("/analytics-stat")
    public ResponseEntity<Map<String, Object>> getAnalyticsStats() {
        try {
            return ResponseEntity.ok(analyticsService.getAnalyticsStats());
        } catch (Exception exception) {
            return ResponseEntity.status(500).body(Map.of("message", "Analytics fetch failed", "error", exception.getMessage()));
        }
    }
}
