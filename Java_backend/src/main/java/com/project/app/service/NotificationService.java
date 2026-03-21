package com.project.app.service;

import com.project.app.entity.Notification;
import com.project.app.repository.NotificationRepository;
import com.project.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public Map<String, Object> getUserNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .filter(notification -> notification.getActor() == Notification.Actor.SYSTEM)
            .toList();

        long unreadCount = notifications.stream().filter(notification -> !Boolean.TRUE.equals(notification.getIsRead())).count();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("unreadCount", unreadCount);
        response.put("data", notifications.stream().map(this::toNotificationPayload).toList());
        return response;
    }

    public Map<String, Object> getAdminNotifications() {
        List<Notification> notifications = notificationRepository.findByActorOrderByCreatedAtDesc(Notification.Actor.USER);
        long unreadCount = notificationRepository.countByActorAndIsReadFalse(Notification.Actor.USER);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("unreadCount", unreadCount);
        response.put("data", notifications.stream().map(this::toNotificationPayload).toList());
        return response;
    }

    public Map<String, Object> markNotificationRead(Long id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", toNotificationPayload(notification));
        return response;
    }

    public Map<String, Object> markUserNotificationsRead(Long userId) {
        notificationRepository.findByUserIdAndIsReadFalse(userId).stream()
            .filter(notification -> notification.getActor() == Notification.Actor.SYSTEM)
            .forEach(notification -> {
                notification.setIsRead(true);
                notificationRepository.save(notification);
            });

        return Map.of("success", true);
    }

    public Map<String, Object> markAdminNotificationsRead() {
        notificationRepository.findByActorOrderByCreatedAtDesc(Notification.Actor.USER).stream()
            .filter(notification -> !Boolean.TRUE.equals(notification.getIsRead()))
            .forEach(notification -> {
                notification.setIsRead(true);
                notificationRepository.save(notification);
            });

        return Map.of("success", true);
    }

    public Map<String, Object> deleteNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notificationRepository.delete(notification);
        return Map.of("success", true, "message", "Notification deleted");
    }

    public Map<String, Object> deleteAllAdminNotifications() {
        notificationRepository.findByActorOrderByCreatedAtDesc(Notification.Actor.USER)
            .forEach(notificationRepository::delete);
        return Map.of("success", true, "message", "All notifications deleted");
    }

    private Map<String, Object> toNotificationPayload(Notification notification) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("_id", notification.getId());
        payload.put("id", notification.getId());
        payload.put("type", notification.getType());
        payload.put("message", notification.getMessage());
        payload.put("actor", notification.getActor().name().toLowerCase());
        payload.put("isRead", notification.getIsRead());
        payload.put("fromAdmin", notification.getFromAdmin());
        payload.put("createdAt", notification.getCreatedAt());
        payload.put("updatedAt", notification.getUpdatedAt());

        Map<String, Object> userPayload = new LinkedHashMap<>();
        userPayload.put("_id", notification.getUser().getId());
        userPayload.put("id", notification.getUser().getId());
        userPayload.put("username", notification.getUser().getUsername());
        userPayload.put("email", notification.getUser().getEmail());
        payload.put("userId", userPayload);

        return payload;
    }
}
