package com.project.app.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "resume_profiles")
public class ResumeProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String headline;

    @Column(length = 2000, nullable = false)
    private String summary;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String location;

    @OneToMany(mappedBy = "resumeProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProfileExperience> experience;

    @OneToMany(mappedBy = "resumeProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProfileEducation> education;

    @OneToMany(mappedBy = "resumeProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProfileSkill> skills;

    @OneToMany(mappedBy = "resumeProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProfileProject> projects;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
