package com.project.app.service;

import com.project.app.entity.ApiMetric;
import com.project.app.repository.ApiMetricRepository;
import com.project.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    @Autowired
    private ApiMetricRepository apiMetricRepository;

    @Autowired
    private UserRepository userRepository;

    public void trackPageView(String page, String userAgent, String ipAddress, Long userId) {
        try {
            ApiMetric metric = new ApiMetric();
            metric.setEndpoint(page);
            metric.setMethod("GET");
            metric.setResponseTime(0L);
            metric.setStatusCode(200);
            metric.setUserAgent(userAgent);
            metric.setIpAddress(ipAddress);
            metric.setTimestamp(LocalDateTime.now());
            
            if (userId != null) {
                userRepository.findById(userId).ifPresent(metric::setUser);
            }
            
            apiMetricRepository.save(metric);
        } catch (Exception e) {
            // Log error but don't fail the request
            System.err.println("Failed to track page view: " + e.getMessage());
        }
    }

    public void trackEvent(String eventType, Map<String, Object> eventData, String userAgent, String ipAddress, Long userId) {
        try {
            ApiMetric metric = new ApiMetric();
            metric.setEndpoint("/event/" + eventType);
            metric.setMethod("POST");
            metric.setResponseTime(0L);
            metric.setStatusCode(200);
            metric.setUserAgent(userAgent);
            metric.setIpAddress(ipAddress);
            metric.setTimestamp(LocalDateTime.now());
            
            if (userId != null) {
                userRepository.findById(userId).ifPresent(metric::setUser);
            }
            
            apiMetricRepository.save(metric);
        } catch (Exception e) {
            // Log error but don't fail the request
            System.err.println("Failed to track event: " + e.getMessage());
        }
    }

    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        List<ApiMetric> userMetrics = apiMetricRepository.findByUserIdOrderByTimestampDesc(userId);
        
        stats.put("totalPageViews", userMetrics.size());
        stats.put("uniquePages", userMetrics.stream()
            .map(ApiMetric::getEndpoint)
            .distinct()
            .count());
        
        stats.put("lastActivity", userMetrics.isEmpty() ? null : userMetrics.get(0).getTimestamp());
        
        return stats;
    }

    public Map<String, Object> getAdminOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        long totalUsers = userRepository.count();
        long totalMetrics = apiMetricRepository.count();
        
        overview.put("totalUsers", totalUsers);
        overview.put("totalPageViews", totalMetrics);
        overview.put("activeUsers", userRepository.countByIsActiveTrue());
        
        // Get recent activity
        List<ApiMetric> recentMetrics = apiMetricRepository.findTop10ByOrderByTimestampDesc();
        overview.put("recentActivity", recentMetrics.stream()
            .map(metric -> Map.of(
                "endpoint", metric.getEndpoint(),
                "method", metric.getMethod(),
                "timestamp", metric.getTimestamp(),
                "user", metric.getUser() != null ? metric.getUser().getUsername() : "Anonymous"
            ))
            .collect(java.util.stream.Collectors.toList()));
        
        return overview;
    }
}
