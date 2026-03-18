package com.project.app.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResumeSummaryRequest {
    private String fullName;
    private Map<String, List<String>> skills;
    private List<Map<String, Object>> education = new ArrayList<>();
    private List<Map<String, Object>> experience = new ArrayList<>();
    private List<Map<String, Object>> certifications = new ArrayList<>();
    private List<Map<String, Object>> projects = new ArrayList<>();
    private String summary;
    private String targetRole;
    private String jobDescription;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Map<String, List<String>> getSkills() {
        return skills;
    }

    public void setSkills(Map<String, List<String>> skills) {
        this.skills = skills;
    }

    public List<Map<String, Object>> getEducation() {
        return education;
    }

    public void setEducation(List<Map<String, Object>> education) {
        this.education = education;
    }

    public List<Map<String, Object>> getExperience() {
        return experience;
    }

    public void setExperience(List<Map<String, Object>> experience) {
        this.experience = experience;
    }

    public List<Map<String, Object>> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<Map<String, Object>> certifications) {
        this.certifications = certifications;
    }

    public List<Map<String, Object>> getProjects() {
        return projects;
    }

    public void setProjects(List<Map<String, Object>> projects) {
        this.projects = projects;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }
}
