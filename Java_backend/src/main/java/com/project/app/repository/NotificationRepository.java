package com.project.app.repository;

import com.project.app.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserIdAndIsReadFalse(Long userId);
    List<Notification> findByActorOrderByCreatedAtDesc(Notification.Actor actor);
    long countByActorAndIsReadFalse(Notification.Actor actor);
    long countByType(String type);
    void deleteByUserId(Long userId);
}
