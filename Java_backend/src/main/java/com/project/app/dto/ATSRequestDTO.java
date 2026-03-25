package com.project.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ATSRequestDTO {
    private String resumeText;
    private String jobDescription; // Mandatory - required for ATS evaluation
}
