package com.project.app.repository;

import com.project.app.entity.TemplateVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemplateVisibilityRepository extends JpaRepository<TemplateVisibility, Long> {
    Optional<TemplateVisibility> findByTemplateId(String templateId);
}
