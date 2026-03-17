package com.project.app.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column(length = 2000)
    private String description;

    @Column
    private String technologies;

    @Column
    private String github;

    @Column
    private String liveLink;

    @Column
    private String other;

    @ManyToOne
    @JoinColumn(name = "resume_id")
    private Resume resume;
}
