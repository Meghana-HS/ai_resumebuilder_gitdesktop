package com.project.app.service;

import com.project.app.dto.ATSAnalysisDTO;
import com.project.app.dto.ATSScoreDTO;
import com.project.app.model.JDAnalysis;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ATSService {

    // Job role keywords mapping
    private static final Map<String, List<String>> ROLE_KEYWORDS = Map.of(
        "Software Engineer", Arrays.asList(
            "java", "python", "javascript", "react", "spring boot", "api", "database", 
            "git", "agile", "scrum", "testing", "debugging", "algorithms", "data structures"
        ),
        "Frontend Developer", Arrays.asList(
            "react", "javascript", "typescript", "html", "css", "vue", "angular", 
            "responsive", "ui", "ux", "webpack", "sass", "bootstrap", "tailwind"
        ),
        "Backend Developer", Arrays.asList(
            "java", "spring boot", "nodejs", "python", "api", "microservices", 
            "mysql", "postgresql", "mongodb", "redis", "docker", "kubernetes", "rest"
        ),
        "Full Stack Developer", Arrays.asList(
            "java", "spring boot", "react", "javascript", "nodejs", "mysql", 
            "api", "frontend", "backend", "full stack", "mongodb", "docker", "git"
        ),
        "Data Scientist", Arrays.asList(
            "python", "machine learning", "tensorflow", "pandas", "numpy", 
            "statistics", "sql", "r", "jupyter", "data analysis", "visualization"
        ),
        "DevOps Engineer", Arrays.asList(
            "docker", "kubernetes", "jenkins", "aws", "azure", "ci/cd", 
            "linux", "terraform", "ansible", "monitoring", "microservices"
        ),
        "Product Manager", Arrays.asList(
            "product management", "agile", "scrum", "roadmap", "stakeholders", 
            "analytics", "user research", "market research", "strategy", "leadership"
        ),
        "UI/UX Designer", Arrays.asList(
            "figma", "sketch", "adobe", "prototyping", "wireframing", "user research", 
            "design systems", "usability", "responsive design", "accessibility"
        )
    );

    // Common ATS-friendly section headers
    private static final List<String> ATS_SECTIONS = Arrays.asList(
        "summary", "experience", "work experience", "employment", "education", 
        "skills", "projects", "certifications", "awards", "languages"
    );

    // Action verbs for experience analysis
    private static final List<String> ACTION_VERBS = Arrays.asList(
        "developed", "implemented", "designed", "created", "managed", "led", 
        "optimized", "improved", "reduced", "increased", "launched", "built"
    );

    public ATSAnalysisDTO analyzeResume(String resumeText, String jobDescription) {
        // Validation - both resume and job description are required
        if (resumeText == null || resumeText.trim().isEmpty() || 
            jobDescription == null || jobDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("Both Resume and Job Description are required for ATS evaluation.");
        }

        // Clean and normalize text
        String cleanResume = cleanText(resumeText);
        String cleanJD = cleanText(jobDescription);
        
        // Analyze job description to get requirements
        JDAnalysis jdAnalysis = analyzeJobDescription(cleanJD);
        String targetRole = jdAnalysis.getDetectedRole();
        
        // Perform detailed analysis
        ATSScoreDTO score = calculateATSScore(cleanResume, cleanJD, jdAnalysis, targetRole);
        
        // Build analysis result
        return ATSAnalysisDTO.builder()
            .atsScore(score.getTotalScore())
            .detectedRole(targetRole)
            .jobDescription(jobDescription)
            .overallEvaluation(generateOverallEvaluation(score.getTotalScore()))
            .keyStrengths(score.getStrengths())
            .areasForImprovement(score.getImprovements())
            .skillsAnalysis(score.getSkillsAnalysis())
            .experienceAnalysis(score.getExperienceAnalysis())
            .formattingAnalysis(score.getFormattingAnalysis())
            .actionableSuggestions(score.getSuggestions())
            .build();
    }

    private JDAnalysis analyzeJobDescription(String jobDescription) {
        String detectedRole = detectRoleFromJD(jobDescription);
        
        List<String> requiredSkills = new ArrayList<>();
        List<String> keyKeywords = new ArrayList<>();
        List<String> experienceRequirements = new ArrayList<>();
        List<String> toolsTechnologies = new ArrayList<>();
        List<String> educationRequirements = new ArrayList<>();
        
        // Extract skills based on detected role
        if (detectedRole != null && ROLE_KEYWORDS.containsKey(detectedRole)) {
            requiredSkills.addAll(ROLE_KEYWORDS.get(detectedRole));
        }
        
        // Extract common tech keywords
        String[] techKeywords = {"java", "python", "javascript", "react", "spring boot", "aws", "docker", "kubernetes", "mysql", "mongodb", "git", "agile", "scrum", "api", "microservices"};
        for (String keyword : techKeywords) {
            if (jobDescription.contains(keyword)) {
                toolsTechnologies.add(keyword);
                keyKeywords.add(keyword);
            }
        }
        
        // Extract experience requirements
        if (Pattern.compile("\\d+\\s*(\\+|years?|yrs?)").matcher(jobDescription).find()) {
            experienceRequirements.add("Experience level specified");
        }
        if (jobDescription.contains("bachelor") || jobDescription.contains("degree")) {
            educationRequirements.add("Bachelor's degree required");
        }
        if (jobDescription.contains("master") || jobDescription.contains("ms")) {
            educationRequirements.add("Master's degree preferred");
        }
        
        return JDAnalysis.builder()
            .detectedRole(detectedRole != null ? detectedRole : "Professional")
            .requiredSkills(requiredSkills)
            .keyKeywords(keyKeywords)
            .experienceRequirements(experienceRequirements)
            .toolsTechnologies(toolsTechnologies)
            .educationRequirements(educationRequirements)
            .build();
    }

    public ATSAnalysisDTO analyzeResume(String resumeText) {
        return analyzeResume(resumeText, null);
    }

    private String cleanText(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s\\-\\.]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String detectRole(String resumeText) {
        Map<String, Integer> roleScores = new HashMap<>();
        
        for (Map.Entry<String, List<String>> entry : ROLE_KEYWORDS.entrySet()) {
            String role = entry.getKey();
            List<String> keywords = entry.getValue();
            
            int score = 0;
            for (String keyword : keywords) {
                if (resumeText.contains(keyword.toLowerCase())) {
                    score++;
                }
            }
            roleScores.put(role, score);
        }
        
        return roleScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Software Engineer");
    }

    private String detectRoleFromJD(String jobDescription) {
        String jdText = jobDescription.toLowerCase();
        Map<String, Integer> roleScores = new HashMap<>();
        
        for (Map.Entry<String, List<String>> entry : ROLE_KEYWORDS.entrySet()) {
            String role = entry.getKey();
            List<String> keywords = entry.getValue();
            
            int score = 0;
            for (String keyword : keywords) {
                if (jdText.contains(keyword.toLowerCase())) {
                    score++;
                }
            }
            roleScores.put(role, score);
        }
        
        return roleScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private String generateJobDescription(String role) {
        switch (role) {
            case "Software Engineer":
                return "We are seeking a skilled Software Engineer to develop high-quality software solutions. " +
                       "Requirements: Strong programming skills in Java/Python, experience with web frameworks, " +
                       "database knowledge, version control, and understanding of software development lifecycle. " +
                       "Must have experience with REST APIs, testing, and agile methodologies.";
                       
            case "Frontend Developer":
                return "Looking for a Frontend Developer to create amazing user experiences. " +
                       "Requirements: Proficiency in React, JavaScript, HTML5, CSS3, responsive design, " +
                       "experience with modern frontend tools, and understanding of UI/UX principles. " +
                       "Must have portfolio of previous work.";
                       
            case "Backend Developer":
                return "Seeking Backend Developer to build robust server-side applications. " +
                       "Requirements: Strong experience with server-side languages, database design, " +
                       "API development, microservices architecture, cloud platforms, and DevOps practices. " +
                       "Experience with system design and scalability is essential.";
                       
            case "Full Stack Developer":
                return "We need a versatile Full Stack Developer comfortable with both frontend and backend. " +
                       "Requirements: Full-stack development experience, proficiency in modern frameworks, " +
                       "database design, API development, and deployment. Must understand end-to-end application " +
                       "development and be able to work independently on projects.";
                       
            case "Data Scientist":
                return "Looking for Data Scientist to extract insights from complex data. " +
                       "Requirements: Strong background in statistics, machine learning, programming (Python/R), " +
                       "data visualization, and experience with ML frameworks. Must have analytical mindset " +
                       "and business acumen.";
                       
            default:
                return "We are seeking a talented professional to join our team. " +
                       "Requirements: Relevant experience, strong technical skills, problem-solving ability, " +
                       "team collaboration, and willingness to learn and adapt to new technologies.";
        }
    }

    private ATSScoreDTO calculateATSScore(String resumeText, String jobDescription, JDAnalysis jdAnalysis, String role) {
        double skillsScore = calculateSkillsScore(resumeText, jdAnalysis);
        double experienceScore = calculateExperienceScore(resumeText, jobDescription);
        double educationScore = calculateEducationScore(resumeText, jdAnalysis);
        double formattingScore = calculateFormattingScore(resumeText);
        
        int totalScore = (int) Math.round(
            skillsScore * 0.40 +      // 40% Skills & Keywords Match
            experienceScore * 0.30 +  // 30% Experience Relevance
            educationScore * 0.15 +   // 15% Education & Certifications
            formattingScore * 0.15    // 15% Formatting & Readability
        );
        
        return ATSScoreDTO.builder()
            .totalScore(totalScore)
            .skillsScore((int) skillsScore)
            .experienceScore((int) experienceScore)
            .educationScore((int) educationScore)
            .formattingScore((int) formattingScore)
            .strengths(generateStrengths(skillsScore, experienceScore, educationScore))
            .improvements(generateImprovements(skillsScore, experienceScore, formattingScore))
            .skillsAnalysis(generateSkillsAnalysis(resumeText, jdAnalysis))
            .experienceAnalysis(generateExperienceAnalysis(resumeText, jobDescription))
            .formattingAnalysis(generateFormattingAnalysis(resumeText))
            .suggestions(generateActionableSuggestions(skillsScore, experienceScore, formattingScore, jdAnalysis))
            .build();
    }

    private double calculateSkillsScore(String resumeText, JDAnalysis jdAnalysis) {
        if (jdAnalysis.getRequiredSkills().isEmpty()) return 50.0;
        
        int matches = 0;
        int totalSkills = jdAnalysis.getRequiredSkills().size();
        
        for (String skill : jdAnalysis.getRequiredSkills()) {
            if (resumeText.contains(skill.toLowerCase())) {
                matches++;
            }
        }
        
        return Math.min(100, (matches * 100.0) / totalSkills);
    }

    private double calculateExperienceScore(String resumeText, String jobDescription) {
        int actionVerbCount = 0;
        for (String verb : ACTION_VERBS) {
            if (resumeText.contains(verb)) {
                actionVerbCount++;
            }
        }
        
        // Check for years of experience mentioned
        boolean hasYears = Pattern.compile("\\d+\\s*(years?|yrs?)").matcher(resumeText).find();
        
        // Check if experience matches JD requirements
        boolean matchesJD = false;
        if (jobDescription.contains("entry level") && resumeText.contains("intern") || resumeText.contains("junior")) {
            matchesJD = true;
        } else if (jobDescription.contains("senior") && resumeText.contains("senior") || resumeText.contains("lead")) {
            matchesJD = true;
        }
        
        double score = Math.min(100, actionVerbCount * 10);
        if (hasYears) score += 15;
        if (matchesJD) score += 15;
        
        return Math.min(100, score);
    }

    private double calculateEducationScore(String resumeText, JDAnalysis jdAnalysis) {
        double score = 50; // Base score
        
        if (resumeText.contains("bachelor") || resumeText.contains("b.s.") || resumeText.contains("b.tech")) {
            score += 30;
        }
        if (resumeText.contains("master") || resumeText.contains("m.s.") || resumeText.contains("m.tech")) {
            score += 20;
        }
        if (resumeText.contains("phd") || resumeText.contains("ph.d")) {
            score += 10;
        }
        
        // Check against JD education requirements
        for (String eduReq : jdAnalysis.getEducationRequirements()) {
            if (eduReq.contains("bachelor") && resumeText.contains("bachelor")) {
                score += 10;
            }
            if (eduReq.contains("master") && resumeText.contains("master")) {
                score += 10;
            }
        }
        
        return Math.min(100, score);
    }

    private double calculateFormattingScore(String resumeText) {
        double score = 50; // Base score
        
        // Check for ATS-friendly sections
        for (String section : ATS_SECTIONS) {
            if (resumeText.contains(section)) {
                score += 5;
            }
        }
        
        // Penalize for formatting issues
        if (resumeText.contains("•") || resumeText.contains("▪") || resumeText.contains("○")) {
            score -= 10; // Non-standard bullets
        }
        
        if (resumeText.length() < 200) {
            score -= 20; // Too short
        }
        
        return Math.max(0, Math.min(100, score));
    }

    private String generateOverallEvaluation(int score) {
        if (score >= 85) return "Excellent match! Your resume strongly aligns with the job requirements.";
        if (score >= 70) return "Good match. Your resume meets most key requirements.";
        if (score >= 55) return "Moderate match. Some improvements needed to better align with the job.";
        return "Poor match. Significant improvements required to meet job requirements.";
    }

    private List<String> generateStrengths(double skillsScore, double experienceScore, double educationScore) {
        List<String> strengths = new ArrayList<>();
        
        if (skillsScore >= 70) strengths.add("Strong skills alignment with job requirements");
        if (experienceScore >= 70) strengths.add("Relevant experience and achievements");
        if (educationScore >= 70) strengths.add("Education meets job requirements");
        if (skillsScore >= 80) strengths.add("Excellent keyword optimization");
        
        if (strengths.isEmpty()) strengths.add("Resume shows potential for improvement");
        
        return strengths;
    }

    private List<String> generateImprovements(double skillsScore, double experienceScore, double formattingScore) {
        List<String> improvements = new ArrayList<>();
        
        if (skillsScore < 60) improvements.add("Add more relevant skills from job description");
        if (experienceScore < 60) improvements.add("Strengthen experience descriptions");
        if (formattingScore < 70) improvements.add("Improve ATS formatting");
        if (skillsScore < 50) improvements.add("Missing key skills for target role");
        
        return improvements;
    }

    private Map<String, Object> generateSkillsAnalysis(String resumeText, JDAnalysis jdAnalysis) {
        List<String> matching = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        
        for (String skill : jdAnalysis.getRequiredSkills()) {
            if (resumeText.contains(skill.toLowerCase())) {
                matching.add(skill);
            } else {
                missing.add(skill);
            }
        }
        
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("matching", matching);
        analysis.put("missing", missing);
        
        return analysis;
    }

    private Map<String, Object> generateExperienceAnalysis(String resumeText, String jobDescription) {
        List<String> relevant = new ArrayList<>();
        List<String> gaps = new ArrayList<>();
        
        if (resumeText.contains("developed") || resumeText.contains("built")) {
            relevant.add("Shows development experience");
        }
        if (resumeText.contains("managed") || resumeText.contains("led")) {
            relevant.add("Demonstrates leadership");
        }
        
        if (!Pattern.compile("\\d+\\s*(years?|yrs?)").matcher(resumeText).find()) {
            gaps.add("Missing specific years of experience");
        }
        if (resumeText.split("\\n").length < 10) {
            gaps.add("Experience section appears too brief");
        }
        
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("relevant", relevant);
        analysis.put("gaps", gaps);
        
        return analysis;
    }

    private Map<String, Object> generateFormattingAnalysis(String resumeText) {
        List<String> issues = new ArrayList<>();
        List<String> fixes = new ArrayList<>();
        
        if (resumeText.contains("•") || resumeText.contains("▪")) {
            issues.add("Non-standard bullet points");
            fixes.add("Use standard bullet points (- or *)");
        }
        
        boolean hasSections = false;
        for (String section : ATS_SECTIONS) {
            if (resumeText.contains(section)) {
                hasSections = true;
                break;
            }
        }
        
        if (!hasSections) {
            issues.add("Missing standard resume sections");
            fixes.add("Add clear section headers");
        }
        
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("issues", issues);
        analysis.put("fixes", fixes);
        
        return analysis;
    }

    private List<String> generateActionableSuggestions(double skillsScore, double experienceScore, double formattingScore, JDAnalysis jdAnalysis) {
        List<String> suggestions = new ArrayList<>();
        
        if (skillsScore < 70) {
            suggestions.add("Add missing skills from job description: " + String.join(", ", jdAnalysis.getRequiredSkills().subList(0, Math.min(3, jdAnalysis.getRequiredSkills().size()))));
        }
        
        if (experienceScore < 70) {
            suggestions.add("Use action verbs and quantify achievements with numbers");
        }
        
        if (formattingScore < 70) {
            suggestions.add("Use standard section headers and simple formatting");
        }
        
        suggestions.add("Tailor resume specifically to this job description");
        suggestions.add("Include keywords from the job requirements");
        
        return suggestions;
    }
}
