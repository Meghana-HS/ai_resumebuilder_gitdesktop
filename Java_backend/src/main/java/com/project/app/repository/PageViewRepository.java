package com.project.app.repository;

import com.project.app.entity.PageView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PageViewRepository extends JpaRepository<PageView, Long> {
    void deleteByUserId(Long userId);
}
