package com.project.app.service;

import com.project.app.dto.AiChatRequest;
import com.project.app.dto.ChatMessageDto;
import com.project.app.dto.CoverLetterRequest;
import com.project.app.dto.ExperienceEnhancementRequest;
import com.project.app.dto.ProjectDescriptionRequest;
import com.project.app.dto.ResumeSkillSuggestionRequest;
import com.project.app.dto.ResumeSummaryRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AiAssistantService {

    public String generateResumeSummary(ResumeSummaryRequest request) {
        String role = firstNonBlank(
            request.getTargetRole(),
            inferRoleFromExperience(request.getExperience()),
            inferRoleFromText(request.getJobDescription()),
            "professional"
        );

        List<String> strengths = collectHighlights(
            flattenSkills(request.getSkills()),
            extractTitles(request.getExperience()),
            extractProjectNames(request.getProjects())
        );

        if (
            strengths.isEmpty() &&
            isBlank(request.getSummary()) &&
            request.getExperience().isEmpty()
        ) {
            throw new IllegalArgumentException(
                "Add at least a target role, experience, skills, or an existing summary before generating AI content."
            );
        }

        String lead = isBlank(request.getFullName())
            ? "Results-driven"
            : request.getFullName().trim() + " is a";

        String firstSentence = String.format(
            "%s %s with experience delivering %s.",
            lead,
            role,
            joinNatural(
                strengths.isEmpty()
                    ? List.of("reliable outcomes in fast-paced environments")
                    : strengths
            )
        );

        String secondSentence = String.format(
            "Known for translating %s into clear business value, cross-functional collaboration, and ATS-friendly communication.",
            request.getExperience().isEmpty()
                ? "technical strengths"
                : "hands-on experience"
        );

        String thirdSentence = String.format(
            "Well suited for %s opportunities that require %s.",
            role,
            joinNatural(
                buildKeywordHighlights(
                    request.getJobDescription(),
                    flattenSkills(request.getSkills())
                )
            )
        );

        return normalizeParagraph(
            firstSentence + " " + secondSentence + " " + thirdSentence
        );
    }

    public String enhanceWorkExperience(ExperienceEnhancementRequest request) {
        if (isBlank(request.getTitle()) || isBlank(request.getCompany())) {
            throw new IllegalArgumentException(
                "Job title and company are required."
            );
        }

        String scope = firstNonBlank(
            request.getDescription(),
            "projects and day-to-day delivery"
        );
        String location = isBlank(request.getLocation())
            ? ""
            : " across " + request.getLocation().trim();

        List<String> bullets = List.of(
            String.format(
                "Led %s initiatives as %s at %s%s, improving delivery quality and operational consistency.",
                inferDomain(scope),
                request.getTitle().trim(),
                request.getCompany().trim(),
                location
            ),
            String.format(
                "Collaborated with stakeholders to prioritize %s, document requirements, and turn complex needs into measurable outcomes.",
                inferFocus(scope)
            ),
            String.format(
                "Strengthened resume impact by framing achievements around ownership, execution, and results from %s to %s.",
                firstNonBlank(request.getStartDate(), "the start of the role"),
                firstNonBlank(request.getEndDate(), "the present")
            )
        );

        return bullets
            .stream()
            .map(b -> "- " + b)
            .collect(Collectors.joining("\n"));
    }

    public String enhanceProjectDescription(ProjectDescriptionRequest request) {
        if (isBlank(request.getName()) || isBlank(request.getDescription())) {
            throw new IllegalArgumentException(
                "Project name and a short description are required."
            );
        }

        String tech = isBlank(request.getTechnologies())
            ? "modern, production-ready tools"
            : request.getTechnologies().trim();
        String focus = inferDomain(
            firstNonBlank(request.getDescription(), tech, "delivery")
        );

        List<String> bullets = List.of(
            String.format(
                "%s built using %s, emphasizing %s quality and maintainability.",
                titleCase(request.getName()),
                tech,
                focus
            ),
            String.format(
                "Delivered end-to-end functionality, including planning, implementation, and validation to ensure the solution matched stakeholder needs."
            ),
            "Highlighted results with metrics (performance, reliability, adoption) to keep the project ATS-friendly and outcome-focused."
        );

        return String.join("\n", bullets);
    }

    public Map<String, Object> suggestSkills(
        ResumeSkillSuggestionRequest request
    ) {
        Set<String> detected = new LinkedHashSet<>(
            flattenSkills(request.getSkills())
        );
        detected.addAll(extractKeywords(request.getJobDescription()));
        detected.addAll(extractKeywords(request.getSummary()));
        detected.addAll(
            extractKeywords(
                String.join(" ", extractTitles(request.getExperience()))
            )
        );

        String role = firstNonBlank(
            request.getTargetRole(),
            request.getJobTitle(),
            inferRoleFromText(request.getJobDescription()),
            "general professional"
        );

        List<String> technical = prioritizeSkills(
            detected,
            List.of(
                "Java",
                "Spring Boot",
                "JavaScript",
                "React",
                "Node.js",
                "SQL",
                "REST APIs",
                "Git",
                "AWS",
                "Python"
            )
        );
        List<String> soft = prioritizeSkills(
            detected,
            List.of(
                "Communication",
                "Leadership",
                "Problem Solving",
                "Stakeholder Management",
                "Collaboration",
                "Time Management"
            )
        );
        List<String> keywords = new ArrayList<>(
            new LinkedHashSet<>(
                Arrays.asList(
                    titleCase(role),
                    "ATS Optimization",
                    "Cross-functional Collaboration",
                    "Process Improvement",
                    "Results-driven",
                    "Impact Metrics"
                )
            )
        );
        keywords.addAll(technical.stream().limit(4).toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("technicalSkills", technical.stream().limit(8).toList());
        response.put("softSkills", soft.stream().limit(6).toList());
        response.put(
            "keywords",
            keywords.stream().distinct().limit(8).toList()
        );
        response.put(
            "atsTips",
            List.of(
                "Use the exact job title or closest standard variant in your headline and summary.",
                "Repeat important keywords naturally in your summary, skills, and experience bullets.",
                "Quantify impact with numbers, percentages, timelines, or team size where possible."
            )
        );
        return response;
    }

    public Map<String, Object> generateCoverLetter(CoverLetterRequest request) {
        String tone = normalizeTone(request.getTone());
        String company = firstNonBlank(request.getCompanyName(), "the company");
        String role = firstNonBlank(request.getJobTitle(), "the role");
        String recipient = firstNonBlank(
            request.getRecipientName(),
            "Hiring Manager"
        );
        String skills = firstNonBlank(
            request.getSkills(),
            "strong problem-solving, communication, and execution skills"
        );
        String experience = firstNonBlank(
            request.getExperience(),
            "experience delivering reliable results in collaborative environments"
        );
        String description = buildJobDescription(
            request.getJobDescription(),
            role,
            company,
            request.getSkills(),
            request.getExperience(),
            request.getFullName()
        );

        String opening = switch (tone) {
            case "formal" -> String.format(
                "Dear %s,\n\nI am writing to express my interest in the %s position at %s. My background includes %s, and I am confident this aligns with what the team needs for the role.",
                recipient,
                role,
                company,
                experience
            );
            case "creative" -> String.format(
                "Dear %s,\n\nWhat draws me to the %s opportunity at %s is the chance to combine %s with meaningful, well-executed work. I bring %s and a bias toward turning ideas into polished outcomes.",
                recipient,
                role,
                company,
                skills,
                experience
            );
            default -> String.format(
                "Dear %s,\n\nI am excited to apply for the %s role at %s. I bring %s and recent results that translate into fast, reliable delivery.",
                recipient,
                role,
                company,
                skills
            );
        };

        String bodyOne = String.format(
            "In recent work, I have built credibility through %s. That experience has strengthened my ability to prioritize effectively, communicate clearly, and contribute to high-quality results with consistency.",
            experience
        );

        String bodyTwo = String.format(
            "I would add value to %s through %s. I focus on clean execution, practical collaboration, and work that reflects both business priorities and end-user expectations.",
            company,
            skills
        );

        String closing = switch (tone) {
            case "formal" -> String.format(
                "Thank you for considering my application. I would welcome the opportunity to discuss how my background can support %s as a %s.",
                company,
                role
            );
            case "creative" -> String.format(
                "I would value the chance to discuss how my approach, energy, and experience can help %s move the %s role forward.",
                company,
                role
            );
            default -> String.format(
                "Thank you for your time and consideration. I would welcome the opportunity to discuss how I can contribute to %s in the %s position.",
                company,
                role
            );
        };

        String fullLetter = String.join(
            "\n\n",
            opening,
            bodyOne,
            bodyTwo,
            closing
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("tone", tone);
        response.put(
            "openingParagraph",
            opening.replaceFirst("^Dear .*?,\\n\\n", "")
        );
        response.put("bodyParagraph1", bodyOne);
        response.put("bodyParagraph2", bodyTwo);
        response.put("closingParagraph", closing);
        response.put("fullLetter", fullLetter);
        response.put(
            "jobDescription",
            refineJobDescription(description, role, company)
        );
        return response;
    }

    private String buildJobDescription(
        String existing,
        String role,
        String company,
        String skills,
        String experience,
        String fullName
    ) {
        if (!isBlank(existing)) {
            return existing.trim();
        }

        String candidate = isBlank(fullName)
            ? "The candidate"
            : fullName.trim();
        String skillsText = isBlank(skills)
            ? "a blend of technical and collaboration strengths"
            : skills;
        String experienceText = isBlank(experience)
            ? "proven ability to deliver reliable results"
            : experience;

        return String.format(
            "%s will serve as %s at %s, applying %s and %s to drive outcomes. The role expects clear communication, ownership from kickoff to delivery, stakeholder alignment, and measurable business impact.",
            candidate,
            titleCase(role),
            company,
            skillsText,
            experienceText
        );
    }

    public Map<String, Object> answerChat(AiChatRequest request) {
        String message = firstNonBlank(request.getMessage(), "").toLowerCase(
            Locale.ENGLISH
        );
        String route = null;
        String mode = "chat";
        String reply;

        if (message.contains("cover letter")) {
            route = "/user/cover-letter";
            mode = "navigation";
            reply =
                "Use the cover letter builder to generate a full draft, choose a tone, and edit each paragraph. Add the target role, company, skills, and experience first for stronger results.";
        } else if (message.contains("cv")) {
            route = "/user/cv";
            mode = "navigation";
            reply =
                "Use the CV builder to strengthen your summary, improve work descriptions, and add skills that match your target role. Keep sections complete and results-focused for better ATS performance.";
        } else if (message.contains("resume")) {
            route = "/user/resume-builder";
            mode = "navigation";
            reply =
                "Start in the resume builder with your target role, strongest skills, and measurable achievements. Use concise action verbs, keyword-aligned skills, and a summary tailored to the job description.";
        } else if (message.contains("ats")) {
            reply =
                "For ATS optimization, match the job title, include priority keywords from the job description, keep headings standard, and quantify results in experience bullets.";
        } else if (message.contains("career")) {
            reply =
                "Career suggestion: target roles that overlap with your strongest skills, recent project work, and the technologies you can discuss confidently in interviews. If you share a target domain, I can narrow that down further.";
        } else {
            String contextHint = summarizeConversation(request.getPrevMsg());
            reply =
                "I can help with resume summaries, experience bullets, ATS keywords, cover letters, and career positioning. " +
                contextHint;
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("text", reply);
        response.put("mode", mode);
        response.put("path", route);
        return response;
    }

    private String summarizeConversation(List<ChatMessageDto> history) {
        if (history == null || history.isEmpty()) {
            return "Tell me whether you want help with a resume, CV, cover letter, or career direction.";
        }
        long userMessages = history
            .stream()
            .filter(m -> "user".equalsIgnoreCase(m.getFrom()))
            .count();
        return userMessages > 0
            ? "Continue with a specific job title or paste a draft section for targeted help."
            : "Tell me whether you want help with a resume, CV, cover letter, or career direction.";
    }

    private List<String> collectHighlights(
        List<String> skills,
        List<String> titles,
        List<String> projects
    ) {
        LinkedHashSet<String> highlights = new LinkedHashSet<>();
        titles
            .stream()
            .limit(2)
            .forEach(t ->
                highlights.add(
                    "impactful " + t.toLowerCase(Locale.ENGLISH) + " work"
                )
            );
        skills
            .stream()
            .limit(3)
            .forEach(skill -> highlights.add(skill));
        projects
            .stream()
            .limit(1)
            .forEach(project -> highlights.add(project + " delivery"));
        return new ArrayList<>(highlights);
    }

    private List<String> buildKeywordHighlights(
        String jobDescription,
        List<String> skills
    ) {
        LinkedHashSet<String> highlights = new LinkedHashSet<>();
        highlights.addAll(
            extractKeywords(jobDescription).stream().limit(3).toList()
        );
        highlights.addAll(skills.stream().limit(2).toList());
        if (highlights.isEmpty()) {
            highlights.add("execution");
            highlights.add("communication");
            highlights.add("measurable impact");
        }
        return new ArrayList<>(highlights);
    }

    private List<String> flattenSkills(Map<String, List<String>> skills) {
        if (skills == null || skills.isEmpty()) {
            return new ArrayList<>();
        }
        return skills
            .values()
            .stream()
            .filter(list -> list != null)
            .flatMap(List::stream)
            .filter(skill -> !isBlank(skill))
            .map(String::trim)
            .distinct()
            .toList();
    }

    private List<String> extractTitles(List<Map<String, Object>> experience) {
        if (experience == null) {
            return List.of();
        }
        return experience
            .stream()
            .map(item ->
                firstNonBlank(
                    asString(item.get("title")),
                    asString(item.get("role"))
                )
            )
            .filter(title -> !isBlank(title))
            .toList();
    }

    private List<String> extractProjectNames(
        List<Map<String, Object>> projects
    ) {
        if (projects == null) {
            return List.of();
        }
        return projects
            .stream()
            .map(item -> asString(item.get("name")))
            .filter(name -> !isBlank(name))
            .toList();
    }

    private String inferRoleFromExperience(
        List<Map<String, Object>> experience
    ) {
        return extractTitles(experience).stream().findFirst().orElse(null);
    }

    private String inferRoleFromText(String text) {
        if (isBlank(text)) {
            return null;
        }
        String lower = text.toLowerCase(Locale.ENGLISH);
        if (
            lower.contains("spring") || lower.contains("java")
        ) return "Java developer";
        if (
            lower.contains("react") || lower.contains("frontend")
        ) return "frontend developer";
        if (lower.contains("node")) return "full-stack developer";
        if (lower.contains("data")) return "data professional";
        if (lower.contains("product")) return "product professional";
        return null;
    }

    private String inferDomain(String description) {
        String lower = description.toLowerCase(Locale.ENGLISH);
        if (lower.contains("api")) return "API and backend";
        if (
            lower.contains("client") ||
            lower.contains("ui") ||
            lower.contains("frontend")
        ) return "frontend and user experience";
        if (lower.contains("data")) return "data-driven";
        return "delivery and process improvement";
    }

    private String inferFocus(String description) {
        String lower = description.toLowerCase(Locale.ENGLISH);
        if (lower.contains("customer")) return "customer-facing priorities";
        if (lower.contains("performance")) return "performance improvements";
        if (lower.contains("automation")) return "automation opportunities";
        return "delivery priorities";
    }

    private List<String> extractKeywords(String source) {
        if (isBlank(source)) {
            return List.of();
        }
        Map<String, List<String>> keywordMap = new LinkedHashMap<>();
        keywordMap.put("Java", List.of("java", "spring", "spring boot"));
        keywordMap.put("React", List.of("react", "frontend", "jsx"));
        keywordMap.put("Node.js", List.of("node", "express", "backend"));
        keywordMap.put("SQL", List.of("sql", "mysql", "postgres"));
        keywordMap.put("AWS", List.of("aws", "cloud", "lambda"));
        keywordMap.put("Python", List.of("python", "pandas", "automation"));
        keywordMap.put("REST APIs", List.of("api", "rest", "integration"));
        keywordMap.put(
            "Communication",
            List.of("communication", "stakeholder", "presentation")
        );
        keywordMap.put("Leadership", List.of("lead", "ownership", "mentor"));
        keywordMap.put(
            "Problem Solving",
            List.of("problem", "debug", "optimize")
        );

        String lower = source.toLowerCase(Locale.ENGLISH);
        return keywordMap
            .entrySet()
            .stream()
            .filter(entry ->
                entry.getValue().stream().anyMatch(lower::contains)
            )
            .map(Map.Entry::getKey)
            .toList();
    }

    private List<String> prioritizeSkills(
        Set<String> detected,
        List<String> defaults
    ) {
        LinkedHashSet<String> suggestions = new LinkedHashSet<>();
        defaults.forEach(skill -> {
            boolean match = detected
                .stream()
                .anyMatch(
                    found ->
                        skill
                            .toLowerCase(Locale.ENGLISH)
                            .contains(found.toLowerCase(Locale.ENGLISH)) ||
                        found
                            .toLowerCase(Locale.ENGLISH)
                            .contains(skill.toLowerCase(Locale.ENGLISH))
                );
            if (match || suggestions.size() < 4) {
                suggestions.add(skill);
            }
        });
        suggestions.addAll(defaults);
        return new ArrayList<>(suggestions);
    }

    private String refineJobDescription(
        String description,
        String role,
        String company
    ) {
        return String.format(
            "%s role at %s focused on %s, cross-functional collaboration, and measurable business impact.",
            titleCase(role),
            company,
            description.replace(".", "").trim()
        );
    }

    private String normalizeTone(String tone) {
        if (isBlank(tone)) {
            return "professional";
        }
        String normalized = tone.trim().toLowerCase(Locale.ENGLISH);
        return switch (normalized) {
            case "formal", "creative" -> normalized;
            default -> "professional";
        };
    }

    private String normalizeParagraph(String text) {
        return text.replaceAll("\\s+", " ").trim();
    }

    private String joinNatural(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "strong execution";
        }
        if (items.size() == 1) {
            return items.get(0);
        }
        if (items.size() == 2) {
            return items.get(0) + " and " + items.get(1);
        }
        return (
            String.join(", ", items.subList(0, items.size() - 1)) +
            ", and " +
            items.get(items.size() - 1)
        );
    }

    private String titleCase(String value) {
        if (isBlank(value)) {
            return "";
        }
        return Arrays.stream(value.trim().split("\\s+"))
            .map(
                word ->
                    word.substring(0, 1).toUpperCase(Locale.ENGLISH) +
                    word.substring(1).toLowerCase(Locale.ENGLISH)
            )
            .collect(Collectors.joining(" "));
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
