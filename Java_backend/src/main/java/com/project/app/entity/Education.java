package com.project.app.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "education")
public class Education {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String school;

    @Column
    private String degree;

    @Column
    private String gpa;

    @Column
    private String startDate;

    @Column
    private String graduationDate;

    @ManyToOne
    @JoinColumn(name = "resume_id")
    private Resume resume;
}
