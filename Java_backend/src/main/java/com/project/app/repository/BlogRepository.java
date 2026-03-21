package com.project.app.repository;

import com.project.app.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long>, JpaSpecificationExecutor<Blog> {
    List<Blog> findByIsPublishedTrueOrderByCreatedAtDesc();
    List<Blog> findByCategory(String category);
}
