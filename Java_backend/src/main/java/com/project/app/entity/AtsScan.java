package com.project.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ats_scans")
public class AtsScan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "resume_profile_id")
    private ResumeProfile resumeProfile;

    @Column
    private String templateId;

    @Column(nullable = false)
    private String jobTitle;

    @Column(nullable = false)
    private Integer overallScore;

    @OneToMany(mappedBy = "atsScan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SectionScore> sectionScores;

    @OneToMany(mappedBy = "atsScan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatchedKeyword> matchedKeywords;

    @OneToMany(mappedBy = "atsScan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MissingKeyword> missingKeywords;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Manual getters/setters to fix compilation issues
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public ResumeProfile getResumeProfile() { return resumeProfile; }
    public void setResumeProfile(ResumeProfile resumeProfile) { this.resumeProfile = resumeProfile; }
    
    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    
    public Integer getOverallScore() { return overallScore; }
    public void setOverallScore(Integer overallScore) { this.overallScore = overallScore; }
    
    public List<SectionScore> getSectionScores() { return sectionScores; }
    public void setSectionScores(List<SectionScore> sectionScores) { this.sectionScores = sectionScores; }
    
    public List<MatchedKeyword> getMatchedKeywords() { return matchedKeywords; }
    public void setMatchedKeywords(List<MatchedKeyword> matchedKeywords) { this.matchedKeywords = matchedKeywords; }
    
    public List<MissingKeyword> getMissingKeywords() { return missingKeywords; }
    public void setMissingKeywords(List<MissingKeyword> missingKeywords) { this.missingKeywords = missingKeywords; }
}
