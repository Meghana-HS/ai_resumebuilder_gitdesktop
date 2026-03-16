package com.project.app.repository;

import com.project.app.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
    List<Template> findByStatus(Template.TemplateStatus status);
    List<Template> findByCategory(String category);
}
