package com.project.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JDAnalysis {
    private String detectedRole;
    private List<String> requiredSkills;
    private List<String> keyKeywords;
    private List<String> experienceRequirements;
    private List<String> toolsTechnologies;
    private List<String> educationRequirements;
}
