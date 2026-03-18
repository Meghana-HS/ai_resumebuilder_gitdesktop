package com.project.app.entity;

import jakarta.persistence.*;







@Entity
@Table(name = "certifications")
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String issuer;

    @Column
    private String date;

    @Column
    private String link;

    @ManyToOne
    @JoinColumn(name = "resume_id")
    private Resume resume;
}
