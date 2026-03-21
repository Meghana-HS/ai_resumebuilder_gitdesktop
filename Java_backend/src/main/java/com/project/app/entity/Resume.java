package com.project.app.entity;

import jakarta.persistence.*;



import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;




@Entity
@Table(name = "resumes")
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Column
    private String fullName;

    @Column
    private String email;

    @Column
    private String linkedin;

    @Column
    private String location;

    @Column
    private String phone;

    @Column(length = 2000)
    private String summary;

    @Column
    private String website;

    /*
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Education> education;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Experience> experience;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects;

    @ElementCollection
    @CollectionTable(name = "resume_skills", joinColumns = @JoinColumn(name = "resume_id"))
    private List<String> technicalSkills;

    @ElementCollection
    @CollectionTable(name = "resume_soft_skills", joinColumns = @JoinColumn(name = "resume_id"))
    private List<String> softSkills;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Certification> certifications;
    */

    /*
    @Column
    private String templateId;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private Template template;
    */

    /*
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    */

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Manual getters to fix compilation issues
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
