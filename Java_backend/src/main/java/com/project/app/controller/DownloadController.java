package com.project.app.controller;

import com.project.app.dto.ApiResponse;
import com.project.app.dto.DownloadRequest;
import com.project.app.entity.Download;
import com.project.app.service.DownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/downloads")
public class DownloadController {

    @Autowired
    private DownloadService downloadService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(authentication.getName());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Download>> createDownload(@RequestBody DownloadRequest downloadRequest) {
        try {
            Long userId = getCurrentUserId();
            Download download = downloadRequest.toDownload();
            Download createdDownload = downloadService.createDownload(userId, download);
            return ResponseEntity.status(201).body(ApiResponse.success("Download created", createdDownload));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create download: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Download>>> getUserDownloads() {
        try {
            Long userId = getCurrentUserId();
            List<Download> downloads = downloadService.getUserDownloads(userId);
            return ResponseEntity.ok(ApiResponse.success("Downloads retrieved", downloads));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to fetch downloads: " + e.getMessage()));
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<Download>>> getRecentActivity() {
        try {
            Long userId = getCurrentUserId();
            List<Download> activity = downloadService.getRecentActivity(userId);
            return ResponseEntity.ok(ApiResponse.success("Recent activity retrieved", activity));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to fetch recent activity: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Download>> getDownloadById(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            Optional<Download> download = downloadService.getDownloadById(id, userId);
            if (download.isPresent()) {
                // Increment views
                Download updatedDownload = downloadService.incrementViews(id, userId);
                return ResponseEntity.ok(ApiResponse.success("Download retrieved", updatedDownload));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to fetch download: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteDownload(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            downloadService.deleteDownload(id, userId);
            return ResponseEntity.ok(ApiResponse.success("Download deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete download: " + e.getMessage()));
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDownloadSummary() {
        try {
            Long userId = getCurrentUserId();
            Map<String, Object> summary = downloadService.getDashboardSummary(userId);
            return ResponseEntity.ok(ApiResponse.success("Download summary retrieved", summary));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to fetch download summary: " + e.getMessage()));
        }
    }
}
