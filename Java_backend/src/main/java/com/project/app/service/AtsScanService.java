package com.project.app.service;

import com.project.app.dto.AtsScanRequest;
import com.project.app.entity.*;
import com.project.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class AtsScanService {

    @Autowired
    private AtsScanRepository atsScanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResumeProfileRepository resumeProfileRepository;

    public AtsScan createScan(Long userId, AtsScanRequest request) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();

        AtsScan scan = new AtsScan();
        scan.setUser(user);
        scan.setJobTitle(request.getJobTitle());
        scan.setTemplateId(request.getTemplateId());

        if (request.getResumeProfileId() != null) {
            Optional<ResumeProfile> profileOpt = resumeProfileRepository.findById(request.getResumeProfileId());
            profileOpt.ifPresent(scan::setResumeProfile);
        }

        // Generate mock ATS scan results
        Random random = new Random();
        scan.setOverallScore(60 + random.nextInt(40)); // Score between 60-100

        // Generate section scores
        List<SectionScore> sectionScores = new ArrayList<>();
        String[] sections = {"Experience", "Education", "Skills", "Projects"};
        for (String section : sections) {
            SectionScore score = new SectionScore();
            score.setSectionName(section);
            score.setScore(50 + random.nextInt(50));
            score.setAtsScan(scan);
            sectionScores.add(score);
        }
        scan.setSectionScores(sectionScores);

        // Generate matched keywords
        List<MatchedKeyword> matchedKeywords = new ArrayList<>();
        String[] keywords = {"Java", "Spring", "React", "JavaScript", "SQL", "Git"};
        for (String keyword : keywords) {
            if (random.nextBoolean()) {
                MatchedKeyword matchedKeyword = new MatchedKeyword();
                matchedKeyword.setKeyword(keyword);
                matchedKeyword.setAtsScan(scan);
                matchedKeywords.add(matchedKeyword);
            }
        }
        scan.setMatchedKeywords(matchedKeywords);

        // Generate missing keywords
        List<MissingKeyword> missingKeywords = new ArrayList<>();
        String[] missing = {"Docker", "Kubernetes", "AWS", "Python", "MongoDB"};
        for (String keyword : missing) {
            if (random.nextBoolean()) {
                MissingKeyword missingKeyword = new MissingKeyword();
                missingKeyword.setKeyword(keyword);
                missingKeyword.setAtsScan(scan);
                missingKeywords.add(missingKeyword);
            }
        }
        scan.setMissingKeywords(missingKeywords);

        return atsScanRepository.save(scan);
    }

    public List<AtsScan> getUserScans(Long userId) {
        return atsScanRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<AtsScan> getScanById(Long scanId, Long userId) {
        Optional<AtsScan> scanOpt = atsScanRepository.findById(scanId);
        if (scanOpt.isPresent() && scanOpt.get().getUser().getId().equals(userId)) {
            return scanOpt;
        }
        return Optional.empty();
    }

    public void deleteScan(Long scanId, Long userId) {
        Optional<AtsScan> scanOpt = atsScanRepository.findById(scanId);
        if (scanOpt.isEmpty() || !scanOpt.get().getUser().getId().equals(userId)) {
            throw new RuntimeException("Scan not found or access denied");
        }
        atsScanRepository.deleteById(scanId);
    }

    public Optional<AtsScan> getLatestScan(Long userId) {
        return atsScanRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
    }

    public Page<AtsScan> getUserScansPaginated(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return atsScanRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Object getScanStatistics(Long userId) {
        List<AtsScan> scans = atsScanRepository.findByUserId(userId);
        
        if (scans.isEmpty()) {
            return new ScanStatistics(0, 0, 0);
        }

        double avgScore = scans.stream()
                .mapToInt(AtsScan::getOverallScore)
                .average()
                .orElse(0.0);

        int highestScore = scans.stream()
                .mapToInt(AtsScan::getOverallScore)
                .max()
                .orElse(0);

        return new ScanStatistics(scans.size(), (int) avgScore, highestScore);
    }

    private static class ScanStatistics {
        public int totalScans;
        public int averageScore;
        public int highestScore;

        public ScanStatistics(int totalScans, int averageScore, int highestScore) {
            this.totalScans = totalScans;
            this.averageScore = averageScore;
            this.highestScore = highestScore;
        }
    }
}
