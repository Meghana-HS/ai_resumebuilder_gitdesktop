package com.project.app.entity;

import jakarta.persistence.*;







@Entity
@Table(name = "section_scores")
public class SectionScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ats_scan_id")
    private AtsScan atsScan;

    @Column(nullable = false)
    private String sectionName;

    @Column(nullable = false)
    private Integer score;
    
    // Manual getters/setters to fix compilation issues
    public AtsScan getAtsScan() { return atsScan; }
    public void setAtsScan(AtsScan atsScan) { this.atsScan = atsScan; }
    
    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
    
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
}
