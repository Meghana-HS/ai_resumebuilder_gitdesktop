package com.project.app.repository;

import com.project.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByIsActiveTrue();
    long countByCreatedAtBefore(java.time.LocalDateTime createdAt);
    long countByLastLoginAfterAndIsAdminFalse(java.time.LocalDateTime lastLogin);
    long countByPlanIgnoreCaseAndIsAdminFalse(String plan);
    long countByPlanIgnoreCaseAndIsActiveTrueAndIsAdminFalse(String plan);
}
