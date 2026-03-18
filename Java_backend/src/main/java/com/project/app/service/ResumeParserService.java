package com.project.app.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeParserService {

    // ── public entry point ────────────────────────────────────────────────
    public Map<String, Object> parseResume(MultipartFile file)
        throws Exception {
        String filename = Objects.requireNonNull(
            file.getOriginalFilename(),
            "filename"
        ).toLowerCase();
        String extension = filename.contains(".")
            ? filename.substring(filename.lastIndexOf('.') + 1)
            : "";

        String text;
        switch (extension) {
            case "pdf" -> text = extractFromPdf(file);
            case "docx" -> text = extractFromDocx(file);
            case "doc" -> text = extractFromDoc(file);
            default -> throw new IllegalArgumentException(
                "Unsupported file type '" +
                    extension +
                    "'. Please upload PDF, DOC, or DOCX."
            );
        }

        if (text == null || text.isBlank()) {
            throw new RuntimeException(
                "Could not extract text from the uploaded file."
            );
        }

        return extractResumeData(text);
    }

    // ═════════════════════════════════════════════════════════════════════
    //  TEXT EXTRACTION
    // ═════════════════════════════════════════════════════════════════════

    private String extractFromPdf(MultipartFile file) throws Exception {
        // PDFBox 3.x removed PDDocument.load(InputStream) — use Loader.loadPDF(byte[])
        byte[] bytes = file.getBytes();
        try (PDDocument doc = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(doc);
        }
    }

    private String extractFromDocx(MultipartFile file) throws Exception {
        try (
            InputStream is = file.getInputStream();
            XWPFDocument doc = new XWPFDocument(is);
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc)
        ) {
            return extractor.getText();
        }
    }

    private String extractFromDoc(MultipartFile file) throws Exception {
        try (
            InputStream is = file.getInputStream();
            HWPFDocument doc = new HWPFDocument(is);
            WordExtractor extractor = new WordExtractor(doc)
        ) {
            return extractor.getText();
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  SECTION SPLITTER  (mirrors splitSections in MERN backend)
    // ═════════════════════════════════════════════════════════════════════

    private Map<String, String> splitSections(String text) {
        Map<String, List<String>> sectionHeaders = new LinkedHashMap<>();
        sectionHeaders.put(
            "experience",
            Arrays.asList(
                "work experience",
                "professional experience",
                "employment",
                "work history",
                "experience"
            )
        );
        sectionHeaders.put(
            "education",
            Arrays.asList(
                "education",
                "academic",
                "qualifications",
                "academics"
            )
        );
        sectionHeaders.put(
            "certifications",
            Arrays.asList(
                "certifications",
                "certification",
                "licenses",
                "achievements",
                "awards"
            )
        );
        sectionHeaders.put(
            "skills",
            Arrays.asList(
                "skills",
                "technical skills",
                "technologies",
                "competencies",
                "expertise"
            )
        );
        sectionHeaders.put(
            "summary",
            Arrays.asList(
                "summary",
                "profile",
                "objective",
                "about",
                "professional summary"
            )
        );
        sectionHeaders.put(
            "projects",
            Arrays.asList(
                "projects",
                "personal projects",
                "key projects",
                "project work"
            )
        );

        Map<String, StringBuilder> sections = new LinkedHashMap<>();
        for (String key : sectionHeaders.keySet())
            sections.put(key, new StringBuilder());
        sections.put("other", new StringBuilder());

        String current = "other";
        for (String rawLine : text.split("\n")) {
            String line = rawLine.trim();
            String lower = line.toLowerCase();

            String matched = null;
            for (Map.Entry<
                String,
                List<String>
            > entry : sectionHeaders.entrySet()) {
                for (String header : entry.getValue()) {
                    if (
                        lower.equals(header) ||
                        lower.startsWith(header + ":") ||
                        lower.startsWith(header + " ")
                    ) {
                        matched = entry.getKey();
                        break;
                    }
                }
                if (matched != null) break;
            }

            if (matched != null) {
                current = matched;
            } else {
                sections.get(current).append(line).append("\n");
            }
        }

        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, StringBuilder> e : sections.entrySet()) {
            result.put(e.getKey(), e.getValue().toString().trim());
        }
        return result;
    }

    // ═════════════════════════════════════════════════════════════════════
    //  MAIN PARSER  (mirrors extractResumeData in MERN backend)
    // ═════════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractResumeData(String text) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("email", null);
        data.put("phone", null);
        data.put("name", null);
        data.put("fullName", null);
        data.put("summary", "");
        data.put(
            "skills",
            Map.of("technical", new ArrayList<>(), "soft", new ArrayList<>())
        );
        data.put("experience", new ArrayList<>());
        data.put("education", new ArrayList<>());
        data.put("certifications", new ArrayList<>());
        data.put("projects", new ArrayList<>());
        data.put("location", null);
        data.put("linkedin", null);
        data.put("website", null);

        // ── Contact info ──────────────────────────────────────────────────

        // Email
        Matcher emailM = Pattern.compile("[\\w.+-]+@[\\w.-]+\\.\\w+").matcher(
            text
        );
        if (emailM.find()) data.put("email", emailM.group());

        // Phone
        Matcher phoneM = Pattern.compile(
            "(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}"
        ).matcher(text);
        if (phoneM.find()) data.put("phone", phoneM.group());

        // LinkedIn
        Matcher liM = Pattern.compile(
            "linkedin\\.com/in/[\\w-]+",
            Pattern.CASE_INSENSITIVE
        ).matcher(text);
        if (liM.find()) data.put("linkedin", liM.group());

        // Website (not linkedin, not email)
        Matcher webM = Pattern.compile(
            "(?:https?://)?(?:www\\.)?[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}(?:/[^\\s]*)?"
        ).matcher(text);
        while (webM.find()) {
            String candidate = webM.group();
            if (!candidate.contains("linkedin") && !candidate.contains("@")) {
                data.put(
                    "website",
                    candidate.startsWith("http")
                        ? candidate
                        : "https://" + candidate
                );
                break;
            }
        }

        // Location  e.g. "San Francisco, CA"
        Matcher locM = Pattern.compile(
            "([A-Za-z\\s]+),\\s*([A-Za-z\\s]{2,})"
        ).matcher(text);
        if (locM.find()) data.put("location", locM.group());

        // ── Name — first short line with only letters/spaces ─────────────
        for (String line : text.split("\n")) {
            String trimmed = line.trim();
            if (
                !trimmed.isEmpty() &&
                trimmed.length() < 50 &&
                trimmed.matches("[A-Za-z\\s.'-]+")
            ) {
                data.put("name", trimmed);
                data.put("fullName", trimmed);
                break;
            }
        }

        // ── Split into sections ───────────────────────────────────────────
        Map<String, String> sections = splitSections(text);

        // ── Summary ───────────────────────────────────────────────────────
        String summaryRaw = sections.getOrDefault("summary", "");
        List<String> summaryLines = new ArrayList<>();
        for (String l : summaryRaw.split("\n")) {
            if (l.trim().length() > 10) summaryLines.add(l.trim());
        }
        if (!summaryLines.isEmpty()) {
            String summaryText = String.join(" ", summaryLines).trim();
            // Strip contact noise
            summaryText = summaryText.replaceAll(
                "[A-Za-z\\s]+,\\s*[A-Za-z\\s]+",
                ""
            );
            summaryText = summaryText.replaceAll(
                "(?i)\\s*\\|\\s*LinkedIn\\s*\\|\\s*GitHub[^|]*",
                ""
            );
            summaryText = summaryText.replaceAll(
                "\\+?\\d{1,3}[-.\\s]?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}",
                ""
            );
            summaryText = summaryText.replaceAll(
                "(?i)linkedin\\.com/in/[\\w-]+",
                ""
            );
            summaryText = summaryText.replaceAll(
                "[\\w.+-]+@[\\w.-]+\\.\\w+",
                ""
            );
            summaryText = summaryText.replaceAll("\\s+", " ").trim();
            summaryText = summaryText.replaceAll("^[\\s|+]+|[\\s|+]+$", "");
            if (summaryText.length() > 500) summaryText = summaryText.substring(
                0,
                500
            );
            data.put("summary", summaryText.length() >= 30 ? summaryText : "");
        }

        // ── Experience ────────────────────────────────────────────────────
        List<Map<String, Object>> experiences = parseExperience(
            sections.getOrDefault("experience", "")
        );
        data.put("experience", experiences);

        // ── Education ─────────────────────────────────────────────────────
        List<Map<String, Object>> education = parseEducation(
            sections.getOrDefault("education", "")
        );
        data.put("education", education);

        // ── Certifications ────────────────────────────────────────────────
        List<Map<String, Object>> certifications = parseCertifications(
            sections.getOrDefault("certifications", "")
        );
        data.put("certifications", certifications);

        // ── Projects ──────────────────────────────────────────────────────
        List<Map<String, Object>> projects = parseProjects(
            sections.getOrDefault("projects", "")
        );
        data.put("projects", projects);

        // ── Skills ────────────────────────────────────────────────────────
        Map<String, List<String>> skills = parseSkills(
            sections.getOrDefault("skills", ""),
            text
        );
        data.put("skills", skills);

        return data;
    }

    // ═════════════════════════════════════════════════════════════════════
    //  EXPERIENCE PARSER
    // ═════════════════════════════════════════════════════════════════════

    private List<Map<String, Object>> parseExperience(String experienceText) {
        List<Map<String, Object>> experiences = new ArrayList<>();
        if (experienceText.isBlank()) return experiences;

        String[] lines = experienceText.split("\n");
        Map<String, Object> current = null;
        StringBuilder descBuilder = new StringBuilder();

        // Date pattern: Jan 2020 - Mar 2023 / 2020 - Present / 01/2020 - 03/2023
        Pattern datePattern = Pattern.compile(
            "(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec|\\d{1,2})[\\s/.-]?(\\d{4})?\\s*[-–]\\s*(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec|present|current|\\d{1,2})[\\s/.-]?(\\d{4})?",
            Pattern.CASE_INSENSITIVE
        );

        // Job title keywords
        Set<String> jobKeywords = new HashSet<>(
            Arrays.asList(
                "engineer",
                "developer",
                "manager",
                "analyst",
                "designer",
                "consultant",
                "intern",
                "lead",
                "architect",
                "specialist",
                "coordinator",
                "director",
                "officer",
                "executive",
                "associate",
                "senior",
                "junior",
                "staff"
            )
        );

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            Matcher dateMatcher = datePattern.matcher(line);
            boolean hasDate = dateMatcher.find();

            // Bullet / description line
            if (
                line.startsWith("•") ||
                line.startsWith("-") ||
                line.startsWith("*")
            ) {
                if (current != null) {
                    String bullet = line.replaceFirst("^[•\\-*]\\s*", "");
                    descBuilder.append(bullet).append("\n");
                }
                continue;
            }

            // Line with a date → likely a job header
            if (hasDate) {
                if (current != null) {
                    current.put("description", descBuilder.toString().trim());
                    experiences.add(current);
                    descBuilder = new StringBuilder();
                }
                current = new LinkedHashMap<>();
                current.put("id", uuid());
                current.put("title", "");
                current.put("company", "");
                current.put("location", "");
                current.put("startDate", extractStartDate(line, dateMatcher));
                current.put("endDate", extractEndDate(line, dateMatcher));
                current.put("description", "");

                // Title / company from the rest of the line
                String beforeDate = line
                    .substring(0, dateMatcher.start())
                    .trim();
                parseTitleAndCompany(beforeDate, current);
                continue;
            }

            // Line that looks like a job title (contains job keyword)
            String lower = line.toLowerCase();
            boolean looksLikeTitle = jobKeywords
                .stream()
                .anyMatch(lower::contains);
            if (looksLikeTitle && line.length() < 80) {
                if (current != null) {
                    current.put("description", descBuilder.toString().trim());
                    experiences.add(current);
                    descBuilder = new StringBuilder();
                }
                current = new LinkedHashMap<>();
                current.put("id", uuid());
                current.put("title", line);
                current.put("company", "");
                current.put("location", "");
                current.put("startDate", "");
                current.put("endDate", "");
                current.put("description", "");
                continue;
            }

            // Company name line (short, title-case, no punctuation except comma)
            if (
                current != null &&
                ((String) current.get("company")).isEmpty() &&
                line.length() < 60 &&
                !line.contains("@")
            ) {
                current.put("company", line);
                continue;
            }

            // Fallback: add as description bullet
            if (current != null) {
                descBuilder.append(line).append("\n");
            }
        }

        if (current != null) {
            current.put("description", descBuilder.toString().trim());
            experiences.add(current);
        }

        return experiences;
    }

    private void parseTitleAndCompany(String text, Map<String, Object> target) {
        if (text.isBlank()) return;
        // Pattern: "Title - Company" or "Title | Company" or "Title @ Company"
        String[] parts = text.split("\\s*[-|@]\\s*", 2);
        target.put("title", parts[0].trim());
        target.put("company", parts.length > 1 ? parts[1].trim() : "");
    }

    private String extractStartDate(String line, Matcher m) {
        m.reset();
        if (m.find()) {
            String g1 = nvl(m.group(1));
            String g2 = nvl(m.group(2));
            return (g1 + " " + g2).trim();
        }
        return "";
    }

    private String extractEndDate(String line, Matcher m) {
        m.reset();
        if (m.find()) {
            String g3 = nvl(m.group(3));
            String g4 = nvl(m.group(4));
            String end = (g3 + " " + g4).trim();
            return end.equalsIgnoreCase("present") ||
                end.equalsIgnoreCase("current")
                ? "Present"
                : end;
        }
        return "Present";
    }

    // ═════════════════════════════════════════════════════════════════════
    //  EDUCATION PARSER
    // ═════════════════════════════════════════════════════════════════════

    private List<Map<String, Object>> parseEducation(String educationText) {
        List<Map<String, Object>> education = new ArrayList<>();
        if (educationText.isBlank()) return education;

        Pattern degreePattern = Pattern.compile(
            "(?i)(bachelor|master|b\\.?sc|m\\.?sc|b\\.?e|m\\.?e|b\\.?tech|m\\.?tech|ph\\.?d|mba|associate|diploma|b\\.?a|m\\.?a|b\\.?com|m\\.?com)[^\\n]*",
            Pattern.CASE_INSENSITIVE
        );
        Pattern datePattern = Pattern.compile(
            "(?i)(\\d{4})\\s*[-–]\\s*(\\d{4}|present|current)",
            Pattern.CASE_INSENSITIVE
        );
        Pattern gpaPattern = Pattern.compile(
            "(?i)(?:gpa|cgpa|grade)[:\\s]+(\\d\\.\\d+)"
        );

        Map<String, Object> current = null;

        for (String rawLine : educationText.split("\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            Matcher degreeMatcher = degreePattern.matcher(line);
            Matcher dateMatcher = datePattern.matcher(line);
            Matcher gpaMatcher = gpaPattern.matcher(line);

            if (degreeMatcher.find()) {
                if (current != null) education.add(current);
                current = newEduEntry();
                current.put("degree", degreeMatcher.group().trim());
                if (dateMatcher.find()) {
                    current.put("startDate", dateMatcher.group(1));
                    current.put("graduationDate", dateMatcher.group(2));
                }
                if (gpaMatcher.find()) current.put("gpa", gpaMatcher.group(1));
                continue;
            }

            if (dateMatcher.find()) {
                if (current == null) {
                    current = newEduEntry();
                }
                current.put("startDate", dateMatcher.group(1));
                current.put("graduationDate", dateMatcher.group(2));
                continue;
            }

            if (gpaMatcher.find()) {
                if (current == null) {
                    current = newEduEntry();
                }
                current.put("gpa", gpaMatcher.group(1));
                continue;
            }

            // Short non-date line → school name or location
            if (line.length() < 80) {
                if (current == null) {
                    current = newEduEntry();
                }
                if (((String) current.get("school")).isEmpty()) {
                    current.put("school", line);
                } else if (
                    ((String) current.get("location")).isEmpty() &&
                    line.matches(".*,.*")
                ) {
                    current.put("location", line);
                }
            }
        }

        if (current != null) education.add(current);
        return education;
    }

    private Map<String, Object> newEduEntry() {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("id", uuid());
        e.put("school", "");
        e.put("degree", "");
        e.put("location", "");
        e.put("startDate", "");
        e.put("graduationDate", "");
        e.put("gpa", "");
        return e;
    }

    // ═════════════════════════════════════════════════════════════════════
    //  CERTIFICATIONS PARSER
    // ═════════════════════════════════════════════════════════════════════

    private List<Map<String, Object>> parseCertifications(String certText) {
        List<Map<String, Object>> certs = new ArrayList<>();
        if (certText.isBlank()) return certs;

        Pattern datePattern = Pattern.compile(
            "(?i)(\\d{4}|jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[\\s,]*(\\d{4})?"
        );
        Pattern issuerPattern = Pattern.compile(
            "(?i)(?:issued by|issuer|by|from)[:\\s]+([^\\n]+)"
        );

        for (String rawLine : certText.split("\n")) {
            String line = rawLine.trim().replaceFirst("^[•\\-*]\\s*", "");
            if (line.isEmpty() || line.length() < 3) continue;

            Map<String, Object> cert = new LinkedHashMap<>();
            cert.put("id", uuid());
            cert.put("name", line);
            cert.put("issuer", "");
            cert.put("date", "");
            cert.put("link", "");

            Matcher dm = datePattern.matcher(line);
            if (dm.find()) {
                cert.put("date", dm.group().trim());
                cert.put("name", line.substring(0, dm.start()).trim());
            }

            Matcher im = issuerPattern.matcher(line);
            if (im.find()) cert.put("issuer", im.group(1).trim());

            certs.add(cert);
        }
        return certs;
    }

    // ═════════════════════════════════════════════════════════════════════
    //  PROJECTS PARSER
    // ═════════════════════════════════════════════════════════════════════

    private List<Map<String, Object>> parseProjects(String projectsText) {
        List<Map<String, Object>> projects = new ArrayList<>();
        if (projectsText.isBlank()) return projects;

        List<String> techKeywords = Arrays.asList(
            "react",
            "node",
            "angular",
            "vue",
            "spring",
            "java",
            "python",
            "javascript",
            "typescript",
            "mongodb",
            "mysql",
            "postgresql",
            "docker",
            "aws",
            "firebase",
            "redis",
            "graphql",
            "express",
            "tailwind",
            "bootstrap",
            "html",
            "css",
            "nextjs",
            "flask",
            "django",
            "kotlin",
            "swift",
            "flutter",
            "dart",
            "go",
            "rust",
            "c\\+\\+",
            "php",
            "ruby",
            "rails",
            "laravel",
            "api",
            "rest"
        );
        Pattern techPattern = Pattern.compile(
            "(?i)" + String.join("|", techKeywords)
        );
        Pattern linkPattern = Pattern.compile(
            "(?i)(https?://[^\\s]+|github\\.com/[^\\s]+)"
        );

        Map<String, Object> current = null;
        StringBuilder descBuilder = new StringBuilder();

        for (String rawLine : projectsText.split("\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            boolean hasTech = techPattern.matcher(line).find();
            boolean hasPipe = line.contains("|");
            boolean isBullet =
                line.startsWith("•") ||
                line.startsWith("-") ||
                line.startsWith("*");

            // Bullet lines → description of current project
            if (isBullet && current != null) {
                descBuilder
                    .append(line.replaceFirst("^[•\\-*]\\s*", ""))
                    .append(" ");
                continue;
            }

            // Link line
            Matcher linkMatcher = linkPattern.matcher(line);
            if (linkMatcher.find() && current != null) {
                String url = linkMatcher.group(1);
                Map<String, String> linkObj = (Map<
                    String,
                    String
                >) current.getOrDefault("link", new LinkedHashMap<>());
                if (url.contains("github")) {
                    ((Map<String, String>) current.get("link")).put(
                        "github",
                        url
                    );
                } else {
                    ((Map<String, String>) current.get("link")).put(
                        "liveLink",
                        url
                    );
                }
                continue;
            }

            // Piped or tech-keyword line → project title/tech row
            if (hasPipe || (hasTech && line.length() < 100)) {
                if (current != null) {
                    current.put("description", descBuilder.toString().trim());
                    projects.add(current);
                    descBuilder = new StringBuilder();
                }
                current = new LinkedHashMap<>();
                current.put("id", uuid());
                current.put("link", new LinkedHashMap<String, String>());

                if (hasPipe) {
                    String[] parts = line.split("\\|", 2);
                    current.put("name", parts[0].trim());
                    current.put(
                        "technologies",
                        parts.length > 1 ? parts[1].trim() : ""
                    );
                } else {
                    current.put("name", line);
                    current.put("technologies", "");
                }
                current.put("description", "");
                continue;
            }

            // Generic description line
            if (current != null) {
                descBuilder.append(line).append(" ");
            } else {
                // First line without recognising a header → treat as project name
                current = new LinkedHashMap<>();
                current.put("id", uuid());
                current.put("name", line);
                current.put("technologies", "");
                current.put("description", "");
                current.put("link", new LinkedHashMap<String, String>());
            }
        }

        if (current != null) {
            current.put("description", descBuilder.toString().trim());
            projects.add(current);
        }

        return projects;
    }

    // ═════════════════════════════════════════════════════════════════════
    //  SKILLS PARSER
    // ═════════════════════════════════════════════════════════════════════

    private Map<String, List<String>> parseSkills(
        String skillsText,
        String fullText
    ) {
        List<String> technical = new ArrayList<>();
        List<String> soft = new ArrayList<>();

        List<String> techKeywords = Arrays.asList(
            "java",
            "python",
            "javascript",
            "typescript",
            "kotlin",
            "swift",
            "c\\+\\+",
            "c#",
            "go",
            "rust",
            "ruby",
            "php",
            "scala",
            "r\\b",
            "react",
            "angular",
            "vue",
            "next\\.?js",
            "node\\.?js",
            "express",
            "spring",
            "django",
            "flask",
            "laravel",
            "rails",
            "html",
            "css",
            "tailwind",
            "bootstrap",
            "sass",
            "sql",
            "mysql",
            "postgresql",
            "mongodb",
            "redis",
            "firebase",
            "elasticsearch",
            "docker",
            "kubernetes",
            "aws",
            "azure",
            "gcp",
            "terraform",
            "jenkins",
            "git",
            "github",
            "bitbucket",
            "jira",
            "confluence",
            "rest",
            "graphql",
            "grpc",
            "api",
            "microservices",
            "linux",
            "bash",
            "powershell",
            "webpack",
            "vite"
        );
        List<String> softKeywords = Arrays.asList(
            "communication",
            "teamwork",
            "leadership",
            "problem.solving",
            "critical.thinking",
            "adaptability",
            "creativity",
            "collaboration",
            "time.management",
            "project.management",
            "agile",
            "scrum",
            "presentation",
            "negotiation",
            "analytical"
        );

        String source = skillsText.isBlank() ? fullText : skillsText;

        // Parse comma/bullet/newline-separated skill lists
        String[] tokens = source.split("[,\n•|]");
        for (String token : tokens) {
            String skill = token.trim().replaceAll("[•\\-*]", "").trim();
            if (skill.isEmpty() || skill.length() > 40) continue;

            boolean isTech = techKeywords
                .stream()
                .anyMatch(kw ->
                    Pattern.compile("(?i)\\b" + kw + "\\b")
                        .matcher(skill)
                        .find()
                );
            boolean isSoft = softKeywords
                .stream()
                .anyMatch(kw ->
                    Pattern.compile("(?i)" + kw).matcher(skill).find()
                );

            if (isTech && !technical.contains(skill)) {
                technical.add(skill);
            } else if (isSoft && !soft.contains(skill)) {
                soft.add(skill);
            } else if (
                !skillsText.isBlank() &&
                !isTech &&
                !isSoft &&
                skill.length() >= 2 &&
                !technical.contains(skill)
            ) {
                // If text came from the skills section and doesn't match known lists,
                // add it to technical (user explicitly listed it as a skill)
                technical.add(skill);
            }
        }

        Map<String, List<String>> skills = new LinkedHashMap<>();
        skills.put("technical", technical);
        skills.put("soft", soft);
        return skills;
    }

    // ═════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═════════════════════════════════════════════════════════════════════

    private String uuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}
