package com.project.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "profile_education")
public class ProfileEducation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "resume_profile_id")
    private ResumeProfile resumeProfile;

    @Column(nullable = false)
    private String degree;

    @Column(nullable = false)
    private LocalDateTime startdate;

    @Column(nullable = false)
    private LocalDateTime enddate;

    @Column(nullable = false)
    private String location;

    @Column(length = 2000, nullable = false)
    private String description;
}
