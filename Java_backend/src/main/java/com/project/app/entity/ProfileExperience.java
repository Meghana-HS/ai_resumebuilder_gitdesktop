package com.project.app.entity;

import jakarta.persistence.*;




import java.time.LocalDateTime;




@Entity
@Table(name = "profile_experience")
public class ProfileExperience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "resume_profile_id")
    private ResumeProfile resumeProfile;

    @Column(nullable = false)
    private String companyname;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private LocalDateTime startdate;

    @Column(nullable = false)
    private LocalDateTime enddate;

    @Column(nullable = false)
    private String location;

    @Column(length = 2000, nullable = false)
    private String description;
}
