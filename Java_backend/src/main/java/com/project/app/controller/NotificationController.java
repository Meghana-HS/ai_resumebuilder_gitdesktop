package com.project.app.controller;

import com.project.app.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> getDefaultUserNotifications() {
        return ResponseEntity.ok(notificationService.getUserNotifications(getCurrentUserId()));
    }

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUserNotifications() {
        return ResponseEntity.ok(notificationService.getUserNotifications(getCurrentUserId()));
    }

    @PutMapping("/user/read-all")
    public ResponseEntity<Map<String, Object>> markUserNotificationsRead() {
        return ResponseEntity.ok(notificationService.markUserNotificationsRead(getCurrentUserId()));
    }

    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> getAdminNotifications() {
        return ResponseEntity.ok(notificationService.getAdminNotifications());
    }

    @PostMapping("/admin/mark-all-read")
    public ResponseEntity<Map<String, Object>> markAdminNotificationsRead() {
        return ResponseEntity.ok(notificationService.markAdminNotificationsRead());
    }

    @DeleteMapping("/admin/delete-all")
    public ResponseEntity<Map<String, Object>> deleteAllAdminNotifications() {
        return ResponseEntity.ok(notificationService.deleteAllAdminNotifications());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markNotificationRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markNotificationRead(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.deleteNotification(id));
    }
}
