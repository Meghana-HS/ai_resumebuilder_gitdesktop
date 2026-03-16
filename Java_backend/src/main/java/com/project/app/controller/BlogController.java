package com.project.app.controller;

import com.project.app.dto.ApiResponse;
import com.project.app.entity.Blog;
import com.project.app.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/blog")
public class BlogController {

    @Autowired
    private BlogService blogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Blog>>> getAllPublishedBlogs() {
        List<Blog> blogs = blogService.getAllPublishedBlogs();
        return ResponseEntity.ok(ApiResponse.success("Blogs retrieved", blogs));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<Blog>>> getAllBlogsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Blog> blogs = blogService.getAllBlogsPaginated(page, size);
        return ResponseEntity.ok(ApiResponse.success("Blogs retrieved", blogs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Blog>> getBlogById(@PathVariable Long id) {
        Optional<Blog> blog = blogService.getBlogById(id);
        return blog.map(value -> ResponseEntity.ok(ApiResponse.success("Blog retrieved", value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Blog>> createBlog(@RequestBody Blog blog) {
        Blog createdBlog = blogService.createBlog(blog);
        return ResponseEntity.status(201).body(ApiResponse.success("Blog created", createdBlog));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Blog>> updateBlog(@PathVariable Long id, @RequestBody Blog blogDetails) {
        try {
            Blog updatedBlog = blogService.updateBlog(id, blogDetails);
            return ResponseEntity.ok(ApiResponse.success("Blog updated", updatedBlog));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBlog(@PathVariable Long id) {
        try {
            blogService.deleteBlog(id);
            return ResponseEntity.ok(ApiResponse.success("Blog deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<Blog>>> getBlogsByCategory(@PathVariable String category) {
        List<Blog> blogs = blogService.getBlogsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success("Blogs by category retrieved", blogs));
    }
}
