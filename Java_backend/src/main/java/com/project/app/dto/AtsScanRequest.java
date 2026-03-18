package com.project.app.dto;

import jakarta.validation.constraints.NotBlank;



public class AtsScanRequest {
    @NotBlank(message = "Job title is required")
    private String jobTitle;
    
    private String templateId;
    private Long resumeProfileId;
    
    // Manual getters to fix compilation issues
    public String getJobTitle() { return jobTitle; }
    public String getTemplateId() { return templateId; }
    public Long getResumeProfileId() { return resumeProfileId; }
}
