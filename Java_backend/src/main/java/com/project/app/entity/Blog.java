package com.project.app.entity;

import jakarta.persistence.*;



import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;




@Entity
@Table(name = "blogs")
public class Blog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String excerpt;

    @Column(length = 5000, nullable = false)
    private String detail;

    @Column(nullable = false)
    private String category;

    @Column
    private String date = "";

    @Column(nullable = false)
    private String image;

    @Column
    private String readTime = "";

    @Column(nullable = false)
    private Boolean isPublished = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Manual getters/setters to fix compilation issues
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getExcerpt() { return excerpt; }
    public void setExcerpt(String excerpt) { this.excerpt = excerpt; }
    
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    
    public String getReadTime() { return readTime; }
    public void setReadTime(String readTime) { this.readTime = readTime; }
    
    public Boolean getIsPublished() { return isPublished; }
    public void setIsPublished(Boolean isPublished) { this.isPublished = isPublished; }
}
