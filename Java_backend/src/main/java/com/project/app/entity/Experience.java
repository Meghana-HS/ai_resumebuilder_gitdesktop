package com.project.app.entity;

import jakarta.persistence.*;







@Entity
@Table(name = "experience")
public class Experience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Column
    private String company;

    @Column(length = 2000)
    private String description;

    @Column
    private String startDate;

    @Column
    private String endDate;

    @ManyToOne
    @JoinColumn(name = "resume_id")
    private Resume resume;
}
