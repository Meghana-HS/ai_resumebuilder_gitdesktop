package com.project.app.service;

import com.project.app.entity.Resume;
import com.project.app.repository.ResumeRepository;
import com.project.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private DownloadService downloadService;

    public Map<String, Object> getDashboardSummary(Long userId) {
        Map<String, Object> summary = new HashMap<>();
        
        // User info
        userRepository.findById(userId).ifPresent(user -> {
            summary.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "isAdmin", user.getIsAdmin(),
                "isActive", user.getIsActive(),
                "lastLogin", user.getLastLogin(),
                "profileViews", user.getProfileViews()
            ));
        });
        
        // Get download statistics
        Map<String, Object> downloadStats = downloadService.getDashboardSummary(userId);
        summary.putAll(downloadStats);
        
        // Resume count - temporarily using findAll
        List<Resume> resumes = resumeRepository.findAll();
        summary.put("resumeCount", resumes.size());
        
        // Recent resumes - temporarily using findAll
        List<Resume> recentResumes = resumeRepository.findAll();
        if (!recentResumes.isEmpty()) {
            Resume latestResume = recentResumes.get(0);
            summary.put("latestResume", Map.of(
                "id", latestResume.getId(),
                "title", latestResume.getTitle() != null ? latestResume.getTitle() : "Untitled Resume",
                "updatedAt", latestResume.getUpdatedAt()
            ));
        }
        
        summary.put("lastUpdated", LocalDateTime.now());
        
        return summary;
    }

    public Map<String, Object> getDashboardStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Resume count - temporarily using findAll
        List<Resume> resumes = resumeRepository.findAll();
        
        stats.put("totalResumes", resumes.size());
        stats.put("activeResumes", resumes.stream().mapToInt(r -> 1).sum());
        
        // Mock data for other stats
        stats.put("profileViews", userRepository.findById(userId)
            .map(user -> user.getProfileViews())
            .orElse(0));
        
        stats.put("atsChecks", 0);
        
        // Get actual download count
        Map<String, Object> downloadStats = downloadService.getDashboardSummary(userId);
        stats.put("downloads", downloadStats.get("totalDownloads"));
        
        return stats;
    }

    public Map<String, Object> getRecentActivity(Long userId) {
        Map<String, Object> activity = new HashMap<>();
        
        // Get recent downloads
        var recentDownloads = downloadService.getRecentActivity(userId);
        
        activity.put("recentActivity", recentDownloads.stream()
            .limit(5)
            .map(download -> Map.of(
                "id", download.getId(),
                "title", download.getName(),
                "type", download.getType().toString(),
                "action", download.getAction().toString(),
                "timestamp", download.getDownloadDate(),
                "description", "Downloaded " + download.getType().toString().toLowerCase()
            ))
            .collect(java.util.stream.Collectors.toList()));
        
        // Get recent resumes - temporarily using findAll
        List<Resume> recentResumes = resumeRepository.findAll();
        
        activity.put("recentResumes", recentResumes.stream()
            .limit(5)
            .map(resume -> Map.of(
                "id", resume.getId(),
                "title", resume.getTitle() != null ? resume.getTitle() : "Untitled Resume",
                "updatedAt", resume.getUpdatedAt(),
                "action", "updated"
            ))
            .collect(java.util.stream.Collectors.toList()));
        
        activity.put("lastActivity", LocalDateTime.now());
        
        return activity;
    }
}
