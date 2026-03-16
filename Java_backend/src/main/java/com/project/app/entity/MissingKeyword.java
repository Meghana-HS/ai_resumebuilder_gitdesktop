package com.project.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "missing_keywords")
public class MissingKeyword {
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
