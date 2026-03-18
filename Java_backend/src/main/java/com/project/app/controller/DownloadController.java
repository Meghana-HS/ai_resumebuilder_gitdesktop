package com.project.app.controller;

import com.project.app.dto.ApiResponse;
import com.project.app.dto.DownloadRequest;
import com.project.app.entity.Download;
import com.project.app.service.DownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/downloads")
public class DownloadController {

    @Autowired
    private DownloadService downloadService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(authentication.getName());
    }

    /** Maps a Download entity to a frontend-friendly Map so enums come back as lowercase strings. */
    private Map<String, Object> toDto(Download d) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",           d.getId());
        map.put("name",         d.getName());

        // type → lowercase with hyphen so the frontend "cover-letter" check works
        String typeStr = "";
        if (d.getType() != null) {
            switch (d.getType()) {
                case COVER_LETTER: typeStr = "cover-letter"; break;
                case CV:           typeStr = "cv";           break;
                case RESUME:       typeStr = "resume";       break;
                default:           typeStr = d.getType().name().toLowerCase();
            }
        }
        map.put("type",         typeStr);

        map.put("format",       d.getFormat()   != null ? d.getFormat().name()   : "PDF");
        map.put("action",       d.getAction()   != null ? d.getAction().name().toLowerCase() : "download");
        map.put("html",         d.getHtml());
        map.put("template",     d.getTemplate());
        map.put("size",         d.getSize());
        map.put("views",        d.getViews());
        map.put("downloadDate", d.getDownloadDate());
        map.put("createdAt",    d.getCreatedAt());
        map.put("updatedAt",    d.getUpdatedAt());
        return map;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createDownload(
            @RequestBody DownloadRequest downloadRequest) {
        try {
            Long userId = getCurrentUserId();
            Download download = downloadRequest.toDownload();
            Download created = downloadService.createDownload(userId, download);
            return ResponseEntity.status(201)
                    .body(ApiResponse.success("Download created", toDto(created)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create download: " + e.getMessage()));
        }
    }

    /**
     * GET /api/downloads
     * Returns only records with action = DOWNLOAD (not VISITED / PREVIEW).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUserDownloads() {
        try {
            Long userId = getCurrentUserId();
            List<Download> downloads = downloadService.getUserDownloadsByAction(
                    userId, Download.Action.DOWNLOAD);

            List<Map<String, Object>> dtos = new ArrayList<>();
            for (Download d : downloads) {
                dtos.add(toDto(d));
            }
            return ResponseEntity.ok(ApiResponse.success("Downloads retrieved", dtos));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to fetch downloads: " + e.getMessage()));
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRecentActivity() {
        try {
            Long userId = getCurrentUserId();
            List<Download> activity = downloadService.getRecentActivity(userId);
            List<Map<String, Object>> dtos = new ArrayList<>();
            for (Download d : activity) dtos.add(toDto(d));
            return ResponseEntity.ok(ApiResponse.success("Recent activity retrieved", dtos));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to fetch recent activity: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDownloadById(
            @PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            Optional<Download> download = downloadService.getDownloadById(id, userId);
            if (download.isPresent()) {
                Download updated = downloadService.incrementViews(id, userId);
                return ResponseEntity.ok(ApiResponse.success("Download retrieved", toDto(updated)));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to fetch download: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteDownload(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            downloadService.deleteDownload(id, userId);
            return ResponseEntity.ok(ApiResponse.success("Download deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete download: " + e.getMessage()));
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDownloadSummary() {
        try {
            Long userId = getCurrentUserId();
            Map<String, Object> summary = downloadService.getDashboardSummary(userId);
            return ResponseEntity.ok(ApiResponse.success("Download summary retrieved", summary));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to fetch download summary: " + e.getMessage()));
        }
    }
}
