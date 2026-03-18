package com.project.app.entity;

import jakarta.persistence.*;







@Entity
@Table(name = "matched_keywords")
public class MatchedKeyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ats_scan_id")
    private AtsScan atsScan;

    @Column(nullable = false)
    private String keyword;
    
    // Manual getters/setters to fix compilation issues
    public AtsScan getAtsScan() { return atsScan; }
    public void setAtsScan(AtsScan atsScan) { this.atsScan = atsScan; }
    
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}
