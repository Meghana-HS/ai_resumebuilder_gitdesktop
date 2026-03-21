package com.project.app.controller;

import com.project.app.dto.ApiResponse;
import com.project.app.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @PostMapping("/api/analytics/page-view")
    public ResponseEntity<ApiResponse<Map<String, String>>> trackPageView(@RequestBody Map<String, Object> data, HttpServletRequest request) {
        String page = (String) data.get("page");
        String route = (String) data.get("route");

        if (page == null || route == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("'page' and 'route' are required"));
        }

        String userAgent = request.getHeader("User-Agent");
        String ipAddress = request.getRemoteAddr();

        Long userId = null;
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
                userId = Long.parseLong(authentication.getName());
            }
        } catch (Exception ignored) {
        }

        analyticsService.trackPageView(page, route, userAgent, ipAddress, userId);
        return ResponseEntity.status(201).body(ApiResponse.success("Page view tracked", Map.of("id", page + ":" + route)));
    }

    @GetMapping("/api/admin/top-pages")
    public ResponseEntity<List<Map<String, Object>>> getTopViewedPages() {
        return ResponseEntity.ok(analyticsService.getTopViewedPages());
    }

    @GetMapping("/api/analytics/user-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("User stats retrieved", analyticsService.getUserStats(userId)));
    }

    @GetMapping("/api/analytics/admin/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminOverview() {
        return ResponseEntity.ok(ApiResponse.success("Admin overview retrieved", analyticsService.getAdminOverview()));
    }
}
