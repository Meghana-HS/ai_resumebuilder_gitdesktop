package com.project.app.repository;

import com.project.app.entity.AtsScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AtsScanRepository extends JpaRepository<AtsScan, Long> {
    List<AtsScan> findByUserId(Long userId);
    Optional<AtsScan> findTopByUserIdOrderByCreatedAtDesc(Long userId);
    List<AtsScan> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<AtsScan> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    void deleteByUserId(Long userId);
}
