package com.project.app.repository;

import com.project.app.entity.Subscription;
import com.project.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUser(User user);
    Optional<Subscription> findByUserAndStatus(User user, Subscription.SubscriptionStatus status);
}
