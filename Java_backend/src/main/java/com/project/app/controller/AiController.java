package com.project.app.controller;

import com.project.app.dto.AiChatRequest;
import com.project.app.dto.ApiResponse;
import com.project.app.dto.CoverLetterRequest;
import com.project.app.dto.ExperienceEnhancementRequest;
import com.project.app.dto.ProjectDescriptionRequest;
import com.project.app.dto.ResumeSkillSuggestionRequest;
import com.project.app.dto.ResumeSummaryRequest;
import com.project.app.service.AiAssistantService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiAssistantService aiAssistantService;

    public AiController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping("/resume/summary")
    public ResponseEntity<
        ApiResponse<Map<String, String>>
    > generateResumeSummary(@RequestBody ResumeSummaryRequest request) {
        String summary = aiAssistantService.generateResumeSummary(request);
        return ResponseEntity.ok(
            ApiResponse.success(
                "Resume summary generated",
                Map.of("content", summary)
            )
        );
    }

    @PostMapping("/resume/experience")
    public ResponseEntity<ApiResponse<Map<String, String>>> enhanceExperience(
        @RequestBody ExperienceEnhancementRequest request
    ) {
        String enhanced = aiAssistantService.enhanceWorkExperience(request);
        return ResponseEntity.ok(
            ApiResponse.success(
                "Work experience enhanced",
                Map.of("content", enhanced)
            )
        );
    }

    @PostMapping("/resume/project")
    public ResponseEntity<ApiResponse<Map<String, String>>> enhanceProject(
        @RequestBody ProjectDescriptionRequest request
    ) {
        String enhanced = aiAssistantService.enhanceProjectDescription(request);
        return ResponseEntity.ok(
            ApiResponse.success(
                "Project description enhanced",
                Map.of("content", enhanced)
            )
        );
    }

    @PostMapping("/resume/skills")
    public ResponseEntity<ApiResponse<Map<String, Object>>> suggestSkills(
        @RequestBody ResumeSkillSuggestionRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(
                "Skill suggestions generated",
                aiAssistantService.suggestSkills(request)
            )
        );
    }

    @PostMapping("/cover-letter")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateCoverLetter(
        @RequestBody CoverLetterRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(
                "Cover letter content generated",
                aiAssistantService.generateCoverLetter(request)
            )
        );
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<Map<String, Object>>> chat(
        @RequestBody AiChatRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(
                "Chat response generated",
                aiAssistantService.answerChat(request)
            )
        );
    }
}
