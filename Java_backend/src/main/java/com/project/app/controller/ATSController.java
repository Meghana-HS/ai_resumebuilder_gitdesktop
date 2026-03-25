package com.project.app.controller;

import com.project.app.dto.ATSAnalysisDTO;
import com.project.app.dto.ATSRequestDTO;
import com.project.app.service.ATSService;
import com.project.app.util.ATSResponseFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ats")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ATSController {

    @Autowired
    private ATSService atsService;

    @PostMapping("/analyze")
    public ResponseEntity<String> analyzeResume(@RequestBody ATSRequestDTO request) {
        try {
            // Validation - both resume and job description are required
            if (request.getResumeText() == null || request.getResumeText().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Error: Both Resume and Job Description are required for ATS evaluation.");
            }
            
            if (request.getJobDescription() == null || request.getJobDescription().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Error: Both Resume and Job Description are required for ATS evaluation.");
            }

            ATSAnalysisDTO analysis = atsService.analyzeResume(
                request.getResumeText(), 
                request.getJobDescription()
            );
            String formattedResponse = ATSResponseFormatter.formatATSResponse(analysis);
            
            return ResponseEntity.ok(formattedResponse);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            // Log the error
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: Failed to analyze resume");
        }
    }

    @PostMapping("/analyze-json")
    public ResponseEntity<ATSAnalysisDTO> analyzeResumeJson(@RequestBody ATSRequestDTO request) {
        try {
            // Validation - both resume and job description are required
            if (request.getResumeText() == null || request.getResumeText().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            if (request.getJobDescription() == null || request.getJobDescription().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            ATSAnalysisDTO analysis = atsService.analyzeResume(
                request.getResumeText(), 
                request.getJobDescription()
            );
            return ResponseEntity.ok(analysis);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            // Log the error
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("ATS Service is running");
    }
}
