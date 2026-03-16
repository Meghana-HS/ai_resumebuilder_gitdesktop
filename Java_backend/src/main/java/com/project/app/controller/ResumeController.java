package com.project.app.controller;

import com.project.app.dto.ApiResponse;
import com.project.app.dto.AtsScanRequest;
import com.project.app.entity.AtsScan;
import com.project.app.entity.Resume;
import com.project.app.service.AtsScanService;
import com.project.app.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private AtsScanService atsScanService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(authentication.getName());
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Resume>> getUserResume() {
        Long userId = getCurrentUserId();
        Optional<Resume> resume = resumeService.getUserResume(userId);
        return resume.map(value -> ResponseEntity.ok(ApiResponse.success("Resume retrieved", value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<AtsScan>> uploadAndAnalyzeResume(@ModelAttribute AtsScanRequest request) {
        Long userId = getCurrentUserId();
        try {
            AtsScan scan = atsScanService.createScan(userId, request);
            return ResponseEntity.status(201).body(ApiResponse.success("Resume uploaded and analyzed", scan));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Resume upload failed"));
        }
    }

    @GetMapping("/scans")
    public ResponseEntity<ApiResponse<Page<AtsScan>>> getUserScans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        Page<AtsScan> scans = atsScanService.getUserScansPaginated(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Scans retrieved", scans));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Object>> getScanStatistics() {
        Long userId = getCurrentUserId();
        Object statistics = atsScanService.getScanStatistics(userId);
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved", statistics));
    }

    @GetMapping("/scans/{id}")
    public ResponseEntity<ApiResponse<AtsScan>> getScanById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Optional<AtsScan> scan = atsScanService.getScanById(id, userId);
        return scan.map(value -> ResponseEntity.ok(ApiResponse.success("Scan retrieved", value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/scans/{id}")
    public ResponseEntity<ApiResponse<String>> deleteScan(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        try {
            atsScanService.deleteScan(id, userId);
            return ResponseEntity.ok(ApiResponse.success("Scan deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<byte[]> downloadResume(@PathVariable String filename) {
        Long userId = getCurrentUserId();
        try {
            byte[] fileContent = resumeService.downloadResume(filename, userId);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .body(fileContent);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<AtsScan>> getLatestScan() {
        Long userId = getCurrentUserId();
        Optional<AtsScan> scan = atsScanService.getLatestScan(userId);
        return scan.map(value -> ResponseEntity.ok(ApiResponse.success("Latest scan retrieved", value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/generate-summary")
    public ResponseEntity<ApiResponse<String>> generateAIResume(@RequestBody String resumeText) {
        // TODO: Implement AI resume generation logic
        return ResponseEntity.ok(ApiResponse.success("AI resume summary generated", "Generated summary"));
    }

    @PostMapping("/cover-letter/generate")
    public ResponseEntity<ApiResponse<String>> generateCoverLetter(@RequestBody String resumeText) {
        // TODO: Implement cover letter generation logic
        return ResponseEntity.ok(ApiResponse.success("Cover letter generated", "Generated cover letter"));
    }

    @PostMapping("/enhance-work-experience")
    public ResponseEntity<ApiResponse<String>> enhanceWorkExperience(@RequestBody String experienceText) {
        // TODO: Implement AI work experience enhancement logic
        return ResponseEntity.ok(ApiResponse.success("Work experience enhanced", "Enhanced experience"));
    }

    @PostMapping("/enhance-project-description")
    public ResponseEntity<ApiResponse<String>> enhanceProjectDescription(@RequestBody String projectText) {
        // TODO: Implement AI project description enhancement logic
        return ResponseEntity.ok(ApiResponse.success("Project description enhanced", "Enhanced project"));
    }

    @PostMapping("/cover-letter/generate-ai")
    public ResponseEntity<ApiResponse<String>> generateAICoverLetter(@RequestBody String resumeText) {
        // TODO: Implement AI cover letter generation logic
        return ResponseEntity.ok(ApiResponse.success("AI cover letter generated", "Generated AI cover letter"));
    }

    @PostMapping("/generate-pdf")
    public ResponseEntity<byte[]> generatePdf(@RequestBody String html) {
        try {
            byte[] pdfBytes = resumeService.generatePdfFromHtml(html);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"resume.pdf\"")
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
