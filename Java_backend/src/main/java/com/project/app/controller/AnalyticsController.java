package com.project.app.controller;

import com.project.app.dto.ApiResponse;
import com.project.app.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(authentication.getName());
    }

    @PostMapping("/page-view")
    public ResponseEntity<ApiResponse<String>> trackPageView(@RequestBody Map<String, Object> data, 
                                                           HttpServletRequest request) {
        try {
            String userAgent = request.getHeader("User-Agent");
            String ipAddress = request.getRemoteAddr();
            String page = (String) data.get("page");
            
            // Try to get user ID if authenticated
            Long userId = null;
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()) {
                    userId = Long.parseLong(authentication.getName());
                }
            } catch (Exception e) {
                // User not authenticated, track anonymously
            }
            
            analyticsService.trackPageView(page, userAgent, ipAddress, userId);
            return ResponseEntity.ok(ApiResponse.success("Page view tracked"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.success("Page view tracking failed but continuing"));
        }
    }

    @PostMapping("/event")
    public ResponseEntity<ApiResponse<String>> trackEvent(@RequestBody Map<String, Object> data,
                                                         HttpServletRequest request) {
        try {
            String eventType = (String) data.get("eventType");
            String userAgent = request.getHeader("User-Agent");
            String ipAddress = request.getRemoteAddr();
            
            Long userId = null;
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()) {
                    userId = Long.parseLong(authentication.getName());
                }
            } catch (Exception e) {
                // User not authenticated, track anonymously
            }
            
            analyticsService.trackEvent(eventType, data, userAgent, ipAddress, userId);
            return ResponseEntity.ok(ApiResponse.success("Event tracked"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.success("Event tracking failed but continuing"));
        }
    }

    @GetMapping("/user-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStats() {
        Long userId = getCurrentUserId();
        Map<String, Object> stats = analyticsService.getUserStats(userId);
        return ResponseEntity.ok(ApiResponse.success("User stats retrieved", stats));
    }

    @GetMapping("/admin/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminOverview() {
        Map<String, Object> overview = analyticsService.getAdminOverview();
        return ResponseEntity.ok(ApiResponse.success("Admin overview retrieved", overview));
    }
}
