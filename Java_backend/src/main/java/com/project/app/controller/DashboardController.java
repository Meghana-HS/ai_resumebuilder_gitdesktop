package com.project.app.controller;

import com.project.app.dto.ApiResponse;
import com.project.app.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(authentication.getName());
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardSummary() {
        Long userId = getCurrentUserId();
        Map<String, Object> summary = dashboardService.getDashboardSummary(userId);
        return ResponseEntity.ok(ApiResponse.success("Dashboard summary retrieved", summary));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        Long userId = getCurrentUserId();
        Map<String, Object> stats = dashboardService.getDashboardStats(userId);
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats retrieved", stats));
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRecentActivity() {
        Long userId = getCurrentUserId();
        Map<String, Object> activity = dashboardService.getRecentActivity(userId);
        return ResponseEntity.ok(ApiResponse.success("Recent activity retrieved", activity));
    }
}
