package com.project.app.service;

import com.project.app.entity.Blog;
import com.project.app.repository.BlogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BlogService {

    @Autowired
    private BlogRepository blogRepository;

    public List<Blog> getAllPublishedBlogs() {
        return blogRepository.findByIsPublishedTrueOrderByCreatedAtDesc();
    }

    public Page<Blog> getAllBlogsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return blogRepository.findAll(pageable);
    }

    public Optional<Blog> getBlogById(Long id) {
        return blogRepository.findById(id);
    }

    public Blog createBlog(Blog blog) {
        return blogRepository.save(blog);
    }

    public Blog updateBlog(Long id, Blog blogDetails) {
        Optional<Blog> blogOpt = blogRepository.findById(id);
        if (blogOpt.isEmpty()) {
            throw new RuntimeException("Blog not found");
        }

        Blog blog = blogOpt.get();
        blog.setTitle(blogDetails.getTitle());
        blog.setExcerpt(blogDetails.getExcerpt());
        blog.setDetail(blogDetails.getDetail());
        blog.setCategory(blogDetails.getCategory());
        blog.setDate(blogDetails.getDate());
        blog.setImage(blogDetails.getImage());
        blog.setReadTime(blogDetails.getReadTime());
        blog.setIsPublished(blogDetails.getIsPublished());

        return blogRepository.save(blog);
    }

    public void deleteBlog(Long id) {
        if (!blogRepository.existsById(id)) {
            throw new RuntimeException("Blog not found");
        }
        blogRepository.deleteById(id);
    }

    public List<Blog> getBlogsByCategory(String category) {
        return blogRepository.findByCategory(category);
    }
}
