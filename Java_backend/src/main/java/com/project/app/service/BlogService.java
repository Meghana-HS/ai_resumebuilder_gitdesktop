package com.project.app.service;

import com.project.app.entity.Blog;
import com.project.app.entity.Notification;
import com.project.app.entity.User;
import com.project.app.repository.BlogRepository;
import com.project.app.repository.NotificationRepository;
import com.project.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class BlogService {

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public Map<String, Object> getBlogs(String category, String search, boolean includeUnpublished) {
        Specification<Blog> spec = Specification.where(null);

        if (!includeUnpublished) {
            spec = spec.and((root, query, cb) -> cb.isTrue(root.get("isPublished")));
        }
        if (category != null && !category.isBlank() && !"All Articles".equalsIgnoreCase(category)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), category));
        }
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("excerpt")), pattern)
            ));
        }

        List<Map<String, Object>> data = blogRepository.findAll(spec).stream()
            .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
            .map(this::toBlogResponse)
            .toList();

        return Map.of("success", true, "data", data);
    }

    public Map<String, Object> getBlogById(Long id) {
        Blog blog = blogRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Blog not found"));
        return Map.of("success", true, "data", toBlogResponse(blog));
    }

    public Map<String, Object> createBlog(Blog blog, Long actorUserId) {
        validateBlog(blog);
        if (blog.getDate() == null) blog.setDate("");
        if (blog.getReadTime() == null) blog.setReadTime("");
        if (blog.getIsPublished() == null) blog.setIsPublished(true);
        Blog created = blogRepository.save(blog);
        createMutationNotification("BLOG_CREATED", "Blog created: " + created.getTitle(), actorUserId);
        return Map.of("success", true, "data", toBlogResponse(created));
    }

    public Map<String, Object> updateBlog(Long id, Blog blogDetails, Long actorUserId) {
        Blog blog = blogRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Blog not found"));

        if (blogDetails.getTitle() != null) blog.setTitle(blogDetails.getTitle());
        if (blogDetails.getExcerpt() != null) blog.setExcerpt(blogDetails.getExcerpt());
        if (blogDetails.getDetail() != null) blog.setDetail(blogDetails.getDetail());
        if (blogDetails.getCategory() != null) blog.setCategory(blogDetails.getCategory());
        if (blogDetails.getDate() != null) blog.setDate(blogDetails.getDate());
        if (blogDetails.getImage() != null) blog.setImage(blogDetails.getImage());
        if (blogDetails.getReadTime() != null) blog.setReadTime(blogDetails.getReadTime());
        if (blogDetails.getIsPublished() != null) blog.setIsPublished(blogDetails.getIsPublished());

        Blog updated = blogRepository.save(blog);
        createMutationNotification("BLOG_UPDATED", "Blog updated: " + updated.getTitle(), actorUserId);
        return Map.of("success", true, "data", toBlogResponse(updated));
    }

    public Map<String, Object> deleteBlog(Long id, Long actorUserId) {
        Blog blog = blogRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Blog not found"));
        blogRepository.delete(blog);
        createMutationNotification("BLOG_DELETED", "Blog deleted: " + blog.getTitle(), actorUserId);
        return Map.of("success", true, "message", "Blog deleted successfully");
    }

    private void validateBlog(Blog blog) {
        if (isBlank(blog.getTitle()) || isBlank(blog.getExcerpt()) || isBlank(blog.getDetail())
            || isBlank(blog.getCategory()) || isBlank(blog.getImage())) {
            throw new IllegalArgumentException("title, excerpt, detail, category, and image are required");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void createMutationNotification(String type, String message, Long userId) {
        if (userId == null) {
            return;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }

        Notification notification = new Notification();
        notification.setType(type);
        notification.setMessage(message);
        notification.setUser(user);
        notification.setActor(Notification.Actor.USER);
        notificationRepository.save(notification);
    }

    private Map<String, Object> toBlogResponse(Blog blog) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", blog.getId());
        response.put("_id", blog.getId());
        response.put("title", blog.getTitle());
        response.put("excerpt", blog.getExcerpt());
        response.put("detail", blog.getDetail());
        response.put("category", blog.getCategory());
        response.put("date", (blog.getDate() != null && !blog.getDate().isBlank())
            ? blog.getDate()
            : blog.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)));
        response.put("image", blog.getImage());
        response.put("readTime", blog.getReadTime());
        response.put("isPublished", blog.getIsPublished());
        response.put("createdAt", blog.getCreatedAt());
        response.put("updatedAt", blog.getUpdatedAt());
        return response;
    }
}
