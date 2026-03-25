package com.project.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ATSAnalysisDTO {
    private int atsScore;
    private String detectedRole;
    private String jobDescription;
    private String overallEvaluation;
    private List<String> keyStrengths;
    private List<String> areasForImprovement;
    private Map<String, Object> skillsAnalysis;
    private Map<String, Object> experienceAnalysis;
    private Map<String, Object> formattingAnalysis;
    private List<String> actionableSuggestions;
}
