package com.project.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AtsScanRequest {
    @NotBlank(message = "Job title is required")
    private String jobTitle;
    
    private String templateId;
    private Long resumeProfileId;
}
