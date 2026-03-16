package com.project.app.service;

import com.project.app.entity.Download;
import com.project.app.entity.User;
import com.project.app.repository.DownloadRepository;
import com.project.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DownloadService {

    @Autowired
    private DownloadRepository downloadRepository;

    @Autowired
    private UserRepository userRepository;

    public Download createDownload(Long userId, Download download) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();
        download.setUser(user);
        download.setViews(1);
        
        return downloadRepository.save(download);
    }

    public List<Download> getUserDownloads(Long userId) {
        return downloadRepository.findByUserIdOrderByDownloadDateDesc(userId);
    }

    public List<Download> getUserDownloadsByType(Long userId, Download.DocumentType type) {
        return downloadRepository.findByUserIdAndTypeOrderByDownloadDateDesc(userId, type);
    }

    public List<Download> getUserDownloadsByAction(Long userId, Download.Action action) {
        return downloadRepository.findByUserIdAndActionOrderByDownloadDateDesc(userId, action);
    }

    public List<Download> getRecentActivity(Long userId) {
        return downloadRepository.findRecentActivity(userId);
    }

    public Optional<Download> getDownloadById(Long id, Long userId) {
        return downloadRepository.findById(id)
                .filter(download -> download.getUser().getId().equals(userId));
    }

    public Download incrementViews(Long id, Long userId) {
        Optional<Download> downloadOpt = getDownloadById(id, userId);
        if (downloadOpt.isEmpty()) {
            throw new RuntimeException("Download not found");
        }

        Download download = downloadOpt.get();
        download.setViews(download.getViews() + 1);
        return downloadRepository.save(download);
    }

    public void deleteDownload(Long id, Long userId) {
        Optional<Download> downloadOpt = getDownloadById(id, userId);
        if (downloadOpt.isEmpty()) {
            throw new RuntimeException("Download not found");
        }
        downloadRepository.deleteById(id);
    }

    public Map<String, Object> getDashboardSummary(Long userId) {
        Map<String, Object> summary = new HashMap<>();
        
        // Total downloads
        Long totalDownloads = downloadRepository.countByUserIdAndAction(userId, Download.Action.DOWNLOAD);
        summary.put("totalDownloads", totalDownloads);
        
        // Downloads by type
        Long resumeDownloads = downloadRepository.countByUserIdAndTypeAndAction(userId, Download.DocumentType.RESUME, Download.Action.DOWNLOAD);
        Long cvDownloads = downloadRepository.countByUserIdAndTypeAndAction(userId, Download.DocumentType.CV, Download.Action.DOWNLOAD);
        Long coverLetterDownloads = downloadRepository.countByUserIdAndTypeAndAction(userId, Download.DocumentType.COVER_LETTER, Download.Action.DOWNLOAD);
        
        summary.put("resumesCreated", resumeDownloads);
        summary.put("cvsCreated", cvDownloads);
        summary.put("coverLettersCreated", coverLetterDownloads);
        
        // Last edited document
        List<Download> recentActivity = getRecentActivity(userId);
        if (!recentActivity.isEmpty()) {
            Download lastEdited = recentActivity.get(0);
            Map<String, Object> lastEditedDoc = new HashMap<>();
            lastEditedDoc.put("title", lastEdited.getName());
            lastEditedDoc.put("updatedAt", lastEdited.getUpdatedAt());
            summary.put("lastEditedDoc", lastEditedDoc);
        }
        
        return summary;
    }

    public Page<Download> getUserDownloadsPaginated(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return downloadRepository.findByUserIdOrderByDownloadDateDesc(userId, pageable);
    }
}
