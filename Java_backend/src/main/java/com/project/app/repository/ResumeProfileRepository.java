package com.project.app.repository;

import com.project.app.entity.ResumeProfile;
import com.project.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeProfileRepository extends JpaRepository<ResumeProfile, Long> {
    List<ResumeProfile> findByUser(User user);
    Optional<ResumeProfile> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
