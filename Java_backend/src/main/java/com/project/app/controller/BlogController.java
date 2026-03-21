package com.project.app.controller;

import com.project.app.service.BlogService;
import com.project.app.entity.Blog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/blog")
public class BlogController {

    @Autowired
    private BlogService blogService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(authentication.getName());
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getBlogs(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "false") boolean includeUnpublished
    ) {
        try {
            return ResponseEntity.ok(blogService.getBlogs(category, search, includeUnpublished));
        } catch (Exception exception) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", exception.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBlogById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(blogService.getBlogById(id));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Blog not found"));
        } catch (Exception exception) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", exception.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createBlog(@RequestBody Blog blog) {
        try {
            return ResponseEntity.status(201).body(blogService.createBlog(blog, getCurrentUserId()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", exception.getMessage()));
        } catch (Exception exception) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", exception.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateBlog(@PathVariable Long id, @RequestBody Blog blogDetails) {
        try {
            return ResponseEntity.ok(blogService.updateBlog(id, blogDetails, getCurrentUserId()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Blog not found"));
        } catch (Exception exception) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", exception.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteBlog(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(blogService.deleteBlog(id, getCurrentUserId()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Blog not found"));
        } catch (Exception exception) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", exception.getMessage()));
        }
    }
}
