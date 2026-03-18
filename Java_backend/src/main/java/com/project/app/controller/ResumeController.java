package com.project.app.controller;

import com.project.app.dto.ApiResponse;
import com.project.app.dto.AtsScanRequest;
import com.project.app.entity.AtsScan;
import com.project.app.entity.Resume;
import com.project.app.service.AtsScanService;
import com.project.app.service.ResumeParserService;
import com.project.app.service.ResumeService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    /**
     * POST /api/resume/ats-scan
     * Accepts a resume file, parses it, computes ATS section scores and returns
     * the exact JSON shape the ATSChecker frontend component expects.
     */
    @PostMapping("/ats-scan")
    public ResponseEntity<ApiResponse<Map<String, Object>>> atsScan(
        @RequestParam("resume") MultipartFile file,
        @RequestParam(value = "jobTitle", required = false, defaultValue = "General") String jobTitle,
        @RequestParam(value = "templateId", required = false) String templateId,
        @RequestParam(value = "resumeprofileId", required = false) String resumeprofileId
    ) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("No file uploaded"));
        }

        try {
            // 1. Parse the resume into structured data
            Map<String, Object> parsed = resumeParserService.parseResume(file);

            // 2. Extract raw text for spell / pronoun analysis on the frontend
            String resumeText = getStr(parsed, "rawText");

            // 3. Build section scores based on what was actually extracted
            List<Map<String, Object>> sectionScores = new ArrayList<>();

            // Contact Info
            Map<String, Object> contactSection = new LinkedHashMap<>();
            boolean hasContact = hasData(parsed, "email") || hasData(parsed, "phone") || hasData(parsed, "fullName");
            contactSection.put("sectionName", "Contact Information");
            contactSection.put("score", hasContact ? 10 : 0);
            contactSection.put("maxScore", 10);
            contactSection.put("status", hasContact ? "ok" : "error");
            contactSection.put("suggestions", hasContact
                ? List.of()
                : List.of("Add your name, email and phone number to the top of your resume."));
            sectionScores.add(contactSection);

            // Summary / Objective
            boolean hasSummary = hasData(parsed, "summary");
            Map<String, Object> summarySection = new LinkedHashMap<>();
            summarySection.put("sectionName", "Summary / Objective");
            summarySection.put("score", hasSummary ? 10 : 3);
            summarySection.put("maxScore", 10);
            summarySection.put("status", hasSummary ? "ok" : "warning");
            summarySection.put("suggestions", hasSummary
                ? List.of()
                : List.of("Add a professional summary or objective statement."));
            sectionScores.add(summarySection);

            // Work Experience
            Object expRaw = parsed.get("experience");
            int expCount = expRaw instanceof List ? ((List<?>) expRaw).size() : 0;
            Map<String, Object> expSection = new LinkedHashMap<>();
            expSection.put("sectionName", "Work Experience");
            expSection.put("score", expCount > 0 ? Math.min(10 + expCount * 5, 25) : 0);
            expSection.put("maxScore", 25);
            expSection.put("status", expCount > 0 ? "ok" : "error");
            expSection.put("suggestions", expCount > 0
                ? List.of()
                : List.of("Add work experience entries with job titles, companies and dates."));
            sectionScores.add(expSection);

            // Education
            Object eduRaw = parsed.get("education");
            int eduCount = eduRaw instanceof List ? ((List<?>) eduRaw).size() : 0;
            Map<String, Object> eduSection = new LinkedHashMap<>();
            eduSection.put("sectionName", "Education");
            eduSection.put("score", eduCount > 0 ? 15 : 0);
            eduSection.put("maxScore", 15);
            eduSection.put("status", eduCount > 0 ? "ok" : "error");
            eduSection.put("suggestions", eduCount > 0
                ? List.of()
                : List.of("Add your educational background including degree and institution."));
            sectionScores.add(eduSection);

            // Skills — parser stores them under parsed["skills"]["technical"] and ["soft"]
            Object skillsRaw = parsed.get("skills");
            int skillCount = 0;
            if (skillsRaw instanceof Map<?,?> skillsMap) {
                Object techRaw = skillsMap.get("technical");
                Object softRaw = skillsMap.get("soft");
                skillCount = (techRaw instanceof List ? ((List<?>) techRaw).size() : 0)
                           + (softRaw instanceof List ? ((List<?>) softRaw).size() : 0);
            }
            Map<String, Object> skillSection = new LinkedHashMap<>();
            skillSection.put("sectionName", "Skills");
            skillSection.put("score", skillCount > 0 ? Math.min(5 + skillCount, 20) : 0);
            skillSection.put("maxScore", 20);
            skillSection.put("status", skillCount > 0 ? "ok" : "warning");
            skillSection.put("suggestions", skillCount > 0
                ? List.of()
                : List.of("List both technical and soft skills relevant to your target role."));
            sectionScores.add(skillSection);

            // File Format Compatibility (always ok since we parsed it successfully)
            Map<String, Object> formatSection = new LinkedHashMap<>();
            String ext = getExtension(file.getOriginalFilename());
            boolean goodFormat = ext.equals("pdf") || ext.equals("docx") || ext.equals("doc");
            formatSection.put("sectionName", "File Format Compatibility");
            formatSection.put("score", goodFormat ? 10 : 0);
            formatSection.put("maxScore", 10);
            formatSection.put("status", goodFormat ? "ok" : "error");
            formatSection.put("suggestions", goodFormat
                ? List.of()
                : List.of("Upload your resume in PDF or DOCX format for best ATS compatibility."));
            sectionScores.add(formatSection);

            // 4. Compute overall score
            int total = sectionScores.stream().mapToInt(s -> (int) s.get("score")).sum();
            int maxTotal = sectionScores.stream().mapToInt(s -> (int) s.get("maxScore")).sum();
            int overallScore = maxTotal > 0 ? (int) Math.round((total * 100.0) / maxTotal) : 0;

            // 5. Build pronoun analysis placeholder (frontend handles display)
            Map<String, Object> pronounAnalysis = new LinkedHashMap<>();
            pronounAnalysis.put("detected", List.of());

            // 5b. Build personalized suggestions
            List<Map<String, Object>> suggestions = buildSuggestions(
                parsed, overallScore, expCount, eduCount, skillCount,
                hasSummary, hasContact);

            // 5c. Build job role recommendations
            List<Map<String, Object>> jobRoles = buildJobRoles(parsed, expCount);

            // 6. Assemble final response
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("overallScore", overallScore);
            result.put("sectionScores", sectionScores);
            result.put("text", resumeText);
            result.put("pronounAnalysis", pronounAnalysis);
            result.put("suggestions",  suggestions);
            result.put("jobRoles",     jobRoles);
            result.put("extractedData", parsed);

            return ResponseEntity.ok(ApiResponse.success("ATS scan complete", result));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("ATS scan failed: " + e.getMessage()));
        }
    }

    /** Returns true when the parsed map has a non-empty value for the given key. */
    @SuppressWarnings("rawtypes")
    private boolean hasData(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return false;
        if (val instanceof String s) return !s.isBlank();
        if (val instanceof List l) return !l.isEmpty();
        return true;
    }

    /** Safely gets a String value from a parsed map. */
    private String getStr(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val instanceof String s ? s : "";
    }

    /**
     * Builds a prioritised list of actionable suggestions.
     * Each suggestion is: { priority, category, message, action }
     *  priority = "critical" | "important" | "tip"
     */
    @SuppressWarnings({"rawtypes","unchecked"})
    private List<Map<String, Object>> buildSuggestions(
            Map<String, Object> parsed, int overallScore, int expCount,
            int eduCount, int skillCount, boolean hasSummary, boolean hasContact) {

        List<Map<String, Object>> suggestions = new ArrayList<>();

        // ── Helper lambda ──────────────────────────────────────────────────
        java.util.function.Supplier<Map<String,Object>> newMap =
                LinkedHashMap::new;

        // Critical — missing essential sections
        if (!hasContact) {
            Map<String,Object> s = newMap.get();
            s.put("priority","critical");
            s.put("category","contact");
            s.put("message","Your resume is missing contact information.");
            s.put("action","Add your full name, email address, and phone number at the top of your resume.");
            suggestions.add(s);
        }
        if (expCount == 0) {
            Map<String,Object> s = newMap.get();
            s.put("priority","critical");
            s.put("category","experience");
            s.put("message","No work experience detected.");
            s.put("action","Add at least one job entry with title, company, dates, and bullet-point responsibilities.");
            suggestions.add(s);
        }
        if (eduCount == 0) {
            Map<String,Object> s = newMap.get();
            s.put("priority","critical");
            s.put("category","education");
            s.put("message","No education section found.");
            s.put("action","Include your highest degree, institution name, and graduation year.");
            suggestions.add(s);
        }
        if (skillCount == 0) {
            Map<String,Object> s = newMap.get();
            s.put("priority","critical");
            s.put("category","skills");
            s.put("message","No skills section detected.");
            s.put("action","Add a dedicated Skills section listing both technical tools and soft skills.");
            suggestions.add(s);
        }

        // Important — weak sections
        if (!hasSummary) {
            Map<String,Object> s = newMap.get();
            s.put("priority","important");
            s.put("category","summary");
            s.put("message","Missing professional summary.");
            s.put("action","Write a 2\u20133 sentence professional summary at the top highlighting your expertise and career goal.");
            suggestions.add(s);
        }
        if (skillCount > 0 && skillCount < 8) {
            Map<String,Object> s = newMap.get();
            s.put("priority","important");
            s.put("category","skills");
            s.put("message","Skills section is sparse (" + skillCount + " skills detected).");
            s.put("action","Aim for 10\u201315 skills covering both technical tools (languages, frameworks) and soft skills (communication, leadership).");
            suggestions.add(s);
        }
        if (expCount > 0 && expCount < 2) {
            Map<String,Object> s = newMap.get();
            s.put("priority","important");
            s.put("category","experience");
            s.put("message","Only one work experience entry found.");
            s.put("action","If possible, add internships, freelance projects, or volunteer work to strengthen your experience section.");
            suggestions.add(s);
        }
        if (!hasData(parsed,"linkedin")) {
            Map<String,Object> s = newMap.get();
            s.put("priority","important");
            s.put("category","contact");
            s.put("message","LinkedIn profile URL is missing.");
            s.put("action","Add your LinkedIn profile URL to the contact section \u2014 recruiters frequently check this.");
            suggestions.add(s);
        }
        if (overallScore < 50 && expCount > 0) {
            Map<String,Object> s = newMap.get();
            s.put("priority","important");
            s.put("category","experience");
            s.put("message","Experience descriptions could be stronger.");
            s.put("action","Start each bullet point with a strong action verb (e.g. Led, Built, Improved, Reduced) and quantify results where possible (e.g. 'Reduced load time by 40%').");
            suggestions.add(s);
        }

        // Tips — polish for scores 50+
        if (overallScore >= 50) {
            Map<String,Object> s = newMap.get();
            s.put("priority","tip");
            s.put("category","keywords");
            s.put("message","Tailor keywords to the specific job description.");
            s.put("action","Copy keywords from the job posting (tools, technologies, soft skills) and naturally incorporate them into your resume sections.");
            suggestions.add(s);
        }
        if (overallScore >= 60) {
            Map<String,Object> s = newMap.get();
            s.put("priority","tip");
            s.put("category","formatting");
            s.put("message","Ensure ATS-friendly formatting.");
            s.put("action","Use simple section headers (Experience, Education, Skills), avoid tables/columns/graphics, and use standard fonts like Arial or Calibri.");
            suggestions.add(s);
        }
        if (overallScore >= 70) {
            Map<String,Object> s = newMap.get();
            s.put("priority","tip");
            s.put("category","experience");
            s.put("message","Quantify your achievements for maximum impact.");
            s.put("action","Replace vague descriptions with numbers: team size, % improvement, revenue impact, number of users served, etc.");
            suggestions.add(s);
        }
        if (!hasData(parsed,"website") && overallScore >= 55) {
            Map<String,Object> s = newMap.get();
            s.put("priority","tip");
            s.put("category","contact");
            s.put("message","No portfolio or personal website listed.");
            s.put("action","Add a link to your GitHub, portfolio site, or personal blog to showcase your work.");
            suggestions.add(s);
        }
        return suggestions;
    }

    /**
     * Maps extracted skills to relevant job roles.
     * Returns roles sorted by match %, limited to top 6.
     * Each entry: { title, matchPercent, matchedSkills, missingSkills, experienceLevel }
     */
    @SuppressWarnings({"rawtypes","unchecked"})
    private List<Map<String, Object>> buildJobRoles(
            Map<String, Object> parsed, int expCount) {

        // ── Collect all candidate skills (lowercase) ──────────────────────
        Set<String> candidateSkills = new java.util.HashSet<>();
        Object skillsRaw = parsed.get("skills");
        if (skillsRaw instanceof Map<?,?> skillsMap) {
            for (Object key : skillsMap.keySet()) {
                Object val = skillsMap.get(key);
                if (val instanceof List<?> list) {
                    for (Object item : list) {
                        if (item instanceof String str) candidateSkills.add(str.toLowerCase().trim());
                        else if (item instanceof Map<?,?> m) {
                            Object n = m.get("name");
                            if (n instanceof String sk) candidateSkills.add(sk.toLowerCase().trim());
                        }
                    }
                }
            }
        }
        // Also scrape raw text for skill keywords
        String rawText = getStr(parsed, "rawText").toLowerCase();

        // ── Role definitions: { title, experience-level, required-skills[] } ──
        Object[][] ROLE_DB = {
            // Frontend
            {"Frontend Developer",       "mid",    new String[]{"javascript","html","css","react","vue","angular","typescript","webpack"}},
            {"React Developer",          "mid",    new String[]{"react","javascript","typescript","redux","jsx","html","css","hooks"}},
            {"Vue.js Developer",         "mid",    new String[]{"vue","javascript","vuex","html","css","typescript"}},
            {"Angular Developer",        "mid",    new String[]{"angular","typescript","javascript","rxjs","html","css"}},
            {"UI/UX Developer",          "mid",    new String[]{"html","css","javascript","figma","react","responsive design","accessibility"}},
            // Backend
            {"Backend Developer",        "mid",    new String[]{"java","python","node.js","spring","django","rest api","sql","microservices"}},
            {"Java Developer",           "mid",    new String[]{"java","spring boot","spring","maven","gradle","sql","junit","hibernate"}},
            {"Spring Boot Developer",    "mid",    new String[]{"java","spring boot","spring","rest api","jpa","hibernate","maven","sql"}},
            {"Node.js Developer",        "mid",    new String[]{"node.js","javascript","express","rest api","mongodb","sql","typescript"}},
            {"Python Developer",         "mid",    new String[]{"python","django","flask","fastapi","rest api","sql","celery"}},
            {"PHP Developer",            "mid",    new String[]{"php","laravel","mysql","html","css","javascript","rest api"}},
            // Full Stack
            {"Full Stack Developer",     "mid",    new String[]{"javascript","react","node.js","sql","rest api","html","css","git"}},
            {"Full Stack Engineer",      "senior", new String[]{"react","node.js","typescript","sql","mongodb","docker","git","rest api"}},
            {"MERN Stack Developer",     "mid",    new String[]{"mongodb","express","react","node.js","javascript","rest api"}},
            {"MEAN Stack Developer",     "mid",    new String[]{"mongodb","express","angular","node.js","javascript","typescript"}},
            // Data / AI / ML
            {"Data Scientist",           "mid",    new String[]{"python","machine learning","tensorflow","pytorch","pandas","numpy","sql","statistics"}},
            {"Data Analyst",             "entry",  new String[]{"sql","python","excel","tableau","power bi","statistics","data visualization"}},
            {"Data Engineer",            "mid",    new String[]{"python","sql","spark","hadoop","etl","aws","airflow","kafka"}},
            {"Machine Learning Engineer","senior", new String[]{"python","machine learning","tensorflow","pytorch","deep learning","nlp","scikit-learn"}},
            {"AI Engineer",              "senior", new String[]{"python","machine learning","deep learning","llm","nlp","pytorch","transformers"}},
            {"Business Intelligence Developer","mid",new String[]{"sql","power bi","tableau","etl","excel","data warehouse","reporting"}},
            // DevOps / Cloud
            {"DevOps Engineer",          "mid",    new String[]{"docker","kubernetes","ci/cd","jenkins","aws","linux","terraform","ansible"}},
            {"Cloud Engineer",           "mid",    new String[]{"aws","azure","gcp","docker","kubernetes","terraform","linux","networking"}},
            {"AWS Developer",            "mid",    new String[]{"aws","lambda","ec2","s3","rds","cloudformation","python","java"}},
            {"Site Reliability Engineer","senior", new String[]{"linux","python","docker","kubernetes","monitoring","prometheus","aws","incident management"}},
            // Mobile
            {"Android Developer",        "mid",    new String[]{"android","java","kotlin","xml","gradle","sqlite","rest api"}},
            {"iOS Developer",            "mid",    new String[]{"ios","swift","objective-c","xcode","cocoapods","rest api"}},
            {"React Native Developer",   "mid",    new String[]{"react native","javascript","typescript","ios","android","redux","rest api"}},
            {"Flutter Developer",        "mid",    new String[]{"flutter","dart","ios","android","firebase","rest api"}},
            // Database / QA / Security
            {"Database Administrator",   "mid",    new String[]{"sql","mysql","postgresql","oracle","mongodb","database design","backup","performance tuning"}},
            {"QA Engineer",              "mid",    new String[]{"testing","selenium","junit","jest","cypress","manual testing","automation","qa","test cases"}},
            {"Cybersecurity Analyst",    "mid",    new String[]{"cybersecurity","network security","owasp","penetration testing","linux","siem","firewalls"}},
            // Management / Soft-skill roles
            {"Project Manager",          "senior", new String[]{"agile","scrum","jira","project management","communication","leadership","risk management"}},
            {"Business Analyst",         "mid",    new String[]{"requirements","analysis","sql","excel","communication","agile","user stories","documentation"}},
            {"Scrum Master",             "mid",    new String[]{"scrum","agile","jira","coaching","sprint planning","retrospective","kanban"}},
            {"Technical Writer",         "entry",  new String[]{"documentation","technical writing","markdown","api","communication","editing","confluence"}},
            {"UI/UX Designer",           "mid",    new String[]{"figma","adobe xd","sketch","ui/ux","wireframing","prototyping","user research","accessibility"}},
        };

        // ── Score each role ───────────────────────────────────────────────
        List<Map<String, Object>> results = new ArrayList<>();

        for (Object[] roleDef : ROLE_DB) {
            String title      = (String) roleDef[0];
            String expLevel   = (String) roleDef[1];
            String[] required = (String[]) roleDef[2];

            List<String> matched = new ArrayList<>();
            List<String> missing = new ArrayList<>();

            for (String skill : required) {
                // Check candidate skills list OR raw text
                boolean found = candidateSkills.contains(skill)
                    || rawText.contains(skill);
                if (found) matched.add(skill);
                else        missing.add(skill);
            }

            if (required.length == 0) continue;
            int matchPct = (int) Math.round((matched.size() * 100.0) / required.length);

            // Only include roles where the candidate matches >= 40%
            if (matchPct < 40) continue;

            Map<String, Object> role = new LinkedHashMap<>();
            role.put("title",           title);
            role.put("matchPercent",    matchPct);
            role.put("experienceLevel", expLevel);
            role.put("matchedSkills",   matched);
            // Limit missingSkills to top 5 to keep response clean
            role.put("missingSkills",   missing.subList(0, Math.min(missing.size(), 5)));
            results.add(role);
        }

        // Sort by matchPercent descending, return top 6
        results.sort((a, b) -> (int) b.get("matchPercent") - (int) a.get("matchPercent"));
        return results.subList(0, Math.min(results.size(), 6));
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
