package com.project.app.util;

import com.project.app.dto.ATSAnalysisDTO;

import java.util.List;
import java.util.Map;

public class ATSResponseFormatter {

    public static String formatATSResponse(ATSAnalysisDTO analysis) {
        StringBuilder response = new StringBuilder();
        
        // ATS Score
        response.append("ATS Score: ").append(analysis.getAtsScore()).append("/100\n\n");
        
        // Detected Role
        response.append("Detected Role:\n");
        response.append(analysis.getDetectedRole()).append("\n\n");
        
        // Overall Evaluation
        response.append("Overall Evaluation:\n");
        response.append(analysis.getOverallEvaluation()).append("\n\n");
        
        // Key Strengths
        response.append("Key Strengths:\n\n");
        List<String> strengths = analysis.getKeyStrengths();
        if (strengths != null && !strengths.isEmpty()) {
            for (String strength : strengths) {
                response.append("* ").append(strength).append("\n");
            }
        } else {
            response.append("* No significant strengths identified\n");
        }
        response.append("\n");
        
        // Areas for Improvement
        response.append("Areas for Improvement:\n\n");
        List<String> improvements = analysis.getAreasForImprovement();
        if (improvements != null && !improvements.isEmpty()) {
            for (String improvement : improvements) {
                response.append("* ").append(improvement).append("\n");
            }
        } else {
            response.append("* No major improvement areas identified\n");
        }
        response.append("\n");
        
        // Skills & Keywords
        response.append("Skills & Keywords:\n\n");
        Map<String, Object> skillsAnalysis = analysis.getSkillsAnalysis();
        if (skillsAnalysis != null) {
            List<String> strongMatches = (List<String>) skillsAnalysis.get("strongMatches");
            List<String> missingWeak = (List<String>) skillsAnalysis.get("missingWeak");
            
            response.append("* Strong Matches: [");
            if (strongMatches != null && !strongMatches.isEmpty()) {
                response.append(String.join(", ", strongMatches));
            }
            response.append("]\n");
            
            response.append("* Missing / Weak: [");
            if (missingWeak != null && !missingWeak.isEmpty()) {
                // Limit to top 10 missing skills for readability
                List<String> limitedMissing = missingWeak.stream().limit(10).toList();
                response.append(String.join(", ", limitedMissing));
                if (missingWeak.size() > 10) {
                    response.append(", ...");
                }
            }
            response.append("]\n");
        }
        response.append("\n");
        
        // Experience Analysis
        response.append("Experience Analysis:\n\n");
        Map<String, Object> experienceAnalysis = analysis.getExperienceAnalysis();
        if (experienceAnalysis != null) {
            List<String> relevant = (List<String>) experienceAnalysis.get("relevant");
            List<String> gaps = (List<String>) experienceAnalysis.get("gaps");
            
            response.append("* Relevant: [");
            if (relevant != null && !relevant.isEmpty()) {
                response.append(String.join(", ", relevant));
            }
            response.append("]\n");
            
            response.append("* Gaps: [");
            if (gaps != null && !gaps.isEmpty()) {
                response.append(String.join(", ", gaps));
            }
            response.append("]\n");
        }
        response.append("\n");
        
        // Formatting & ATS Check
        response.append("Formatting & ATS Check:\n\n");
        Map<String, Object> formattingAnalysis = analysis.getFormattingAnalysis();
        if (formattingAnalysis != null) {
            List<String> issues = (List<String>) formattingAnalysis.get("issues");
            List<String> fixes = (List<String>) formattingAnalysis.get("fixes");
            
            response.append("* Issues: [");
            if (issues != null && !issues.isEmpty()) {
                response.append(String.join(", ", issues));
            } else {
                response.append("None");
            }
            response.append("]\n");
            
            response.append("* Fixes: [");
            if (fixes != null && !fixes.isEmpty()) {
                response.append(String.join(", ", fixes));
            } else {
                response.append("None");
            }
            response.append("]\n");
        }
        response.append("\n");
        
        // Actionable Suggestions
        response.append("Actionable Suggestions:\n\n");
        List<String> suggestions = analysis.getActionableSuggestions();
        if (suggestions != null && !suggestions.isEmpty()) {
            int count = 1;
            for (String suggestion : suggestions) {
                response.append(count).append(". ").append(suggestion).append("\n");
                count++;
            }
        } else {
            response.append("1. No specific suggestions available\n");
        }
        
        return response.toString();
    }
}
