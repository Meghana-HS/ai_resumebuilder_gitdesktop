package com.project.app.entity;

import jakarta.persistence.*;







@Entity
@Table(name = "profile_skills")
public class ProfileSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "resume_profile_id")
    private ResumeProfile resumeProfile;

    @Column(nullable = false)
    private String skill;
}
