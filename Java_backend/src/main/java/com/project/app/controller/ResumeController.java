package com.project.app.controller;

import com.project.app.dto.ApiResponse;
import com.project.app.dto.AtsScanRequest;
import com.project.app.entity.AtsScan;
import com.project.app.entity.Resume;
import com.project.app.service.AtsScanService;
import com.project.app.service.ResumeParserService;
import com.project.app.service.ResumeService;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private AtsScanService atsScanService;

    @Autowired
    private ResumeParserService resumeParserService;

    private Long getCurrentUserId() {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(authentication.getName());
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Resume>> getUserResume() {
        Long userId = getCurrentUserId();
        Optional<Resume> resume = resumeService.getUserResume(userId);
        return resume
            .map(value ->
                ResponseEntity.ok(
                    ApiResponse.success("Resume retrieved", value)
                )
            )
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/upload")
    public ResponseEntity<
        ApiResponse<Map<String, Object>>
    > uploadAndAnalyzeResume(
        @RequestParam("resume") MultipartFile file,
        @RequestParam(value = "jobTitle", required = false) String jobTitle,
        @RequestParam(value = "templateId", required = false) String templateId,
        @RequestParam(
            value = "resumeprofileId",
            required = false
        ) String resumeprofileId
    ) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("No file uploaded")
            );
        }

        try {
            // Parse resume file → extract structured data (same as MERN backend)
            Map<String, Object> extractedData = resumeParserService.parseResume(
                file
            );

            // Build response matching the shape the frontend reads:
            // res.data?.data?.extractedData
            Map<String, Object> responseData = new LinkedHashMap<>();
            responseData.put("extractedData", extractedData);
            responseData.put("filename", file.getOriginalFilename());
            responseData.put(
                "fileType",
                getExtension(file.getOriginalFilename())
            );
            responseData.put("templateId", templateId);

            return ResponseEntity.ok(
                ApiResponse.success(
                    "Resume uploaded and imported successfully",
                    responseData
                )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error(
                    "Failed to upload and analyze resume: " + e.getMessage()
                )
            );
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    @GetMapping("/scans")
    public ResponseEntity<ApiResponse<Page<AtsScan>>> getUserScans(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = getCurrentUserId();
        Page<AtsScan> scans = atsScanService.getUserScansPaginated(
            userId,
            page,
            size
        );
        return ResponseEntity.ok(ApiResponse.success("Scans retrieved", scans));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Object>> getScanStatistics() {
        Long userId = getCurrentUserId();
        Object statistics = atsScanService.getScanStatistics(userId);
        return ResponseEntity.ok(
            ApiResponse.success("Statistics retrieved", statistics)
        );
    }

    @GetMapping("/scans/{id}")
    public ResponseEntity<ApiResponse<AtsScan>> getScanById(
        @PathVariable Long id
    ) {
        Long userId = getCurrentUserId();
        Optional<AtsScan> scan = atsScanService.getScanById(id, userId);
        return scan
            .map(value ->
                ResponseEntity.ok(ApiResponse.success("Scan retrieved", value))
            )
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/scans/{id}")
    public ResponseEntity<ApiResponse<String>> deleteScan(
        @PathVariable Long id
    ) {
        Long userId = getCurrentUserId();
        try {
            atsScanService.deleteScan(id, userId);
            return ResponseEntity.ok(ApiResponse.success("Scan deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<byte[]> downloadResume(
        @PathVariable String filename
    ) {
        Long userId = getCurrentUserId();
        try {
            byte[] fileContent = resumeService.downloadResume(filename, userId);
            return ResponseEntity.ok()
                .header(
                    "Content-Disposition",
                    "attachment; filename=\"" + filename + "\""
                )
                .body(fileContent);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<AtsScan>> getLatestScan() {
        Long userId = getCurrentUserId();
        Optional<AtsScan> scan = atsScanService.getLatestScan(userId);
        return scan
            .map(value ->
                ResponseEntity.ok(
                    ApiResponse.success("Latest scan retrieved", value)
                )
            )
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/generate-summary")
    public ResponseEntity<ApiResponse<String>> generateAIResume(
        @RequestBody String resumeText
    ) {
        // TODO: Implement AI resume generation logic
        return ResponseEntity.ok(
            ApiResponse.success(
                "AI resume summary generated",
                "Generated summary"
            )
        );
    }

    @PostMapping("/cover-letter/generate")
    public ResponseEntity<ApiResponse<String>> generateCoverLetter(
        @RequestBody Map<String, Object> requestBody
    ) {
        try {
            String sectionType = (String) requestBody.getOrDefault(
                "sectionType",
                "openingParagraph"
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> jobDetails = requestBody.containsKey(
                "jobDetails"
            )
                ? (Map<String, Object>) requestBody.get("jobDetails")
                : new HashMap<>();

            String jobTitle = (String) jobDetails.getOrDefault(
                "jobTitle",
                "this position"
            );
            String companyName = (String) jobDetails.getOrDefault(
                "companyName",
                "your company"
            );
            String fullName = (String) jobDetails.getOrDefault("fullName", "I");
            String skills = (String) jobDetails.getOrDefault("skills", "");
            String experience = (String) jobDetails.getOrDefault(
                "experience",
                ""
            );

            String generated = buildCoverLetterSection(
                sectionType,
                jobTitle,
                companyName,
                fullName,
                skills,
                experience
            );

            return ResponseEntity.ok(
                ApiResponse.success("Cover letter section generated", generated)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(
                    "Failed to generate cover letter content: " + e.getMessage()
                )
            );
        }
    }

    /** Generates a template paragraph for the requested cover letter section. */
    private String buildCoverLetterSection(
        String sectionType,
        String jobTitle,
        String companyName,
        String fullName,
        String skills,
        String experience
    ) {
        String skillsClause = (skills != null && !skills.isBlank())
            ? skills
            : "a strong technical and analytical skill set";

        switch (sectionType) {
            case "openingParagraph" -> {
                return String.format(
                    "I am writing to express my enthusiastic interest in the %s position at %s. " +
                        "With a proven background in %s, I am confident that my expertise and " +
                        "passion for delivering high-quality results make me an excellent candidate " +
                        "for this role. I am excited about the opportunity to contribute to your team " +
                        "and help %s achieve its goals.",
                    jobTitle,
                    companyName,
                    skillsClause,
                    companyName
                );
            }
            case "bodyParagraph1" -> {
                String expClause = (experience != null && !experience.isBlank())
                    ? experience
                    : "my previous roles";
                return String.format(
                    "In %s, I have consistently demonstrated the ability to solve complex problems " +
                        "and deliver impactful solutions. My hands-on experience with %s has equipped " +
                        "me with the technical proficiency and collaborative mindset needed to thrive " +
                        "in a fast-paced environment. I have successfully led and contributed to " +
                        "projects that improved efficiency, increased quality, and drove measurable " +
                        "business outcomes.",
                    expClause,
                    skillsClause
                );
            }
            case "bodyParagraph2" -> {
                return String.format(
                    "My technical skills include %s, which I apply with a focus on clean, " +
                        "maintainable, and scalable solutions. I am a quick learner who embraces new " +
                        "technologies and methodologies, and I thrive when collaborating with " +
                        "cross-functional teams. I believe that combining strong technical foundations " +
                        "with clear communication is key to delivering exceptional results.",
                    skillsClause
                );
            }
            case "closingParagraph" -> {
                return String.format(
                    "I am particularly drawn to %s because of its commitment to innovation and " +
                        "excellence. I would welcome the opportunity to discuss how my background, " +
                        "skills, and enthusiasm align with the goals of your team. Thank you for " +
                        "considering my application — I look forward to the possibility of " +
                        "contributing to %s and am available at your earliest convenience for an " +
                        "interview.",
                    companyName,
                    companyName
                );
            }
            default -> {
                return String.format(
                    "I am excited to apply for the %s role at %s and bring my skills in %s " +
                        "to your team.",
                    jobTitle,
                    companyName,
                    skillsClause
                );
            }
        }
    }

    @PostMapping("/enhance-work-experience")
    public ResponseEntity<ApiResponse<String>> enhanceWorkExperience(
        @RequestBody String experienceText
    ) {
        // TODO: Implement AI work experience enhancement logic
        return ResponseEntity.ok(
            ApiResponse.success(
                "Work experience enhanced",
                "Enhanced experience"
            )
        );
    }

    @PostMapping("/enhance-project-description")
    public ResponseEntity<ApiResponse<String>> enhanceProjectDescription(
        @RequestBody String projectText
    ) {
        // TODO: Implement AI project description enhancement logic
        return ResponseEntity.ok(
            ApiResponse.success(
                "Project description enhanced",
                "Enhanced project"
            )
        );
    }

    @PostMapping("/cover-letter/generate-ai")
    public ResponseEntity<ApiResponse<String>> generateAICoverLetter(
        @RequestBody String resumeText
    ) {
        // TODO: Implement AI cover letter generation logic
        return ResponseEntity.ok(
            ApiResponse.success(
                "AI cover letter generated",
                "Generated AI cover letter"
            )
        );
    }

    @PostMapping("/generate-pdf")
    public ResponseEntity<byte[]> generatePdf(@RequestBody String html) {
        try {
            byte[] pdfBytes = resumeService.generatePdfFromHtml(html);
            return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header(
                    "Content-Disposition",
                    "attachment; filename=\"resume.pdf\""
                )
                .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
