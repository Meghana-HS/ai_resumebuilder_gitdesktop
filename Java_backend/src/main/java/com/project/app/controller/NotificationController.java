package com.project.app.controller;

import com.project.app.dto.ApiResponse;
import com.project.app.entity.Notification;
import com.project.app.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(authentication.getName());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getUserNotifications() {
        Long userId = getCurrentUserId();
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", notifications));
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<Notification>>> getUnreadNotifications() {
        Long userId = getCurrentUserId();
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success("Unread notifications retrieved", notifications));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Notification>> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        try {
            Notification notification = notificationService.markAsRead(id, userId);
            return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<String>> markAllAsRead() {
        Long userId = getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteNotification(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        try {
            notificationService.deleteNotification(id, userId);
            return ResponseEntity.ok(ApiResponse.success("Notification deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Notification>> createNotification(@RequestBody Notification notification) {
        Notification createdNotification = notificationService.createNotification(notification);
        return ResponseEntity.status(201).body(ApiResponse.success("Notification created", createdNotification));
    }
}
