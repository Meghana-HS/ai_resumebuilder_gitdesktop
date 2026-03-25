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
public class ATSScoreDTO {
    private int totalScore;
    private int skillsScore;
    private int experienceScore;
    private int projectsScore;
    private int formattingScore;
    private int educationScore;
    private List<String> strengths;
    private List<String> improvements;
    private Map<String, Object> skillsAnalysis;
    private Map<String, Object> experienceAnalysis;
    private Map<String, Object> formattingAnalysis;
    private List<String> suggestions;
}
