package com.project.app.service;

import com.project.app.entity.Notification;
import com.project.app.entity.Template;
import com.project.app.entity.User;
import com.project.app.repository.NotificationRepository;
import com.project.app.repository.TemplateRepository;
import com.project.app.repository.UserRepository;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TemplateService {

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${server.port:8081}")
    private String serverPort;

    public List<Map<String, Object>> getTemplates(String status) {
        List<Template> templates = (status != null && !status.isBlank())
            ? templateRepository.findByStatus(Template.TemplateStatus.valueOf(status.trim().toUpperCase()))
            : templateRepository.findAll();

        return templates.stream()
            .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
            .map(this::toTemplateResponse)
            .toList();
    }

    public Map<String, Object> getTemplateById(Long id) {
        Template template = templateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Template not found"));
        return toTemplateResponse(template);
    }

    public Map<String, Object> getTemplateHtml(Long id) {
        Template template = templateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Template not found"));

        Path filePath = Paths.get(template.getFilePath()).normalize();
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("File not found on server");
        }

        String fileName = filePath.getFileName().toString().toLowerCase();
        try {
            String html;
            if (fileName.endsWith(".html")) {
                html = Files.readString(filePath, StandardCharsets.UTF_8);
            } else if (fileName.endsWith(".docx")) {
                html = docxToHtml(filePath);
            } else {
                html = "<html><body><p>Preview not available for this file type.</p></body></html>";
            }
            return Map.of("html", html);
        } catch (IOException exception) {
            throw new IllegalStateException("Parsing failed");
        }
    }

    public Map<String, Object> uploadTemplate(String name, String category, MultipartFile templateFile, MultipartFile thumbnail, Long userId) {
        if (templateFile == null || thumbnail == null) {
            throw new IllegalArgumentException("Template file & thumbnail required");
        }

        Template template = new Template();
        template.setName(name);
        template.setCategory(category != null && !category.isBlank() ? category : "Modern");
        template.setFilePath(storeFile(templateFile));
        template.setPreviewimage(storeFile(thumbnail));
        template.setStatus(Template.TemplateStatus.PENDING);

        Template saved = templateRepository.save(template);
        createNotification("TEMPLATE_CREATED", "New template submitted: " + name + " (" + saved.getCategory() + ")", userId, Notification.Actor.USER, false);
        createNotification("TEMPLATE_CREATED", "Your template has been submitted for approval", userId, Notification.Actor.SYSTEM, false);

        return Map.of(
            "msg", "Template uploaded & pending approval",
            "template", toTemplateResponse(saved)
        );
    }

    public Map<String, Object> approveTemplate(Long id) {
        Template template = templateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Template not found"));
        template.setStatus(Template.TemplateStatus.APPROVED);
        Template saved = templateRepository.save(template);

        return Map.of(
            "msg", "Template approved",
            "template", toTemplateResponse(saved)
        );
    }

    public Map<String, Object> updateTemplate(Long id, String name, String category, MultipartFile templateFile, MultipartFile thumbnail, Long userId) {
        Template template = templateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Template not found"));

        if (templateFile != null && !templateFile.isEmpty()) {
            deleteIfExists(template.getFilePath());
            template.setFilePath(storeFile(templateFile));
        }
        if (thumbnail != null && !thumbnail.isEmpty()) {
            deleteIfExists(template.getPreviewimage());
            template.setPreviewimage(storeFile(thumbnail));
        }
        if (name != null && !name.isBlank()) {
            template.setName(name);
        }
        if (category != null && !category.isBlank()) {
            template.setCategory(category);
        }

        Template saved = templateRepository.save(template);
        createNotification("TEMPLATE_UPDATED", "Template updated", userId, Notification.Actor.USER, false);

        return Map.of(
            "msg", "Template updated successfully",
            "template", toTemplateResponse(saved)
        );
    }

    public Map<String, Object> deleteTemplate(Long id, Long userId) {
        Template template = templateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Template not found"));

        deleteIfExists(template.getFilePath());
        deleteIfExists(template.getPreviewimage());
        templateRepository.delete(template);
        createNotification("TEMPLATE_DELETED", "Template deleted", userId, Notification.Actor.USER, false);

        return Map.of("msg", "Template deleted successfully");
    }

    private Map<String, Object> toTemplateResponse(Template template) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_id", template.getId());
        response.put("id", template.getId());
        response.put("name", template.getName());
        response.put("description", template.getDescription());
        response.put("previewimage", template.getPreviewimage());
        response.put("filePath", template.getFilePath());
        response.put("status", template.getStatus().name().toLowerCase());
        response.put("category", template.getCategory());
        response.put("createdAt", template.getCreatedAt());
        response.put("updatedAt", template.getUpdatedAt());
        response.put("fileUrl", buildFileUrl(template.getFilePath()));
        response.put("imageUrl", buildFileUrl(template.getPreviewimage()));
        return response;
    }

    private String buildFileUrl(String filePath) {
        return "http://localhost:" + serverPort + "/uploads/templates/" + Paths.get(filePath).getFileName();
    }

    private String storeFile(MultipartFile file) {
        try {
            Path uploadDir = Paths.get("uploads", "templates").toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file.bin";
            String safeName = System.currentTimeMillis() + "-" + originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path target = uploadDir.resolve(safeName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString();
        } catch (IOException exception) {
            throw new IllegalStateException("Server Error");
        }
    }

    private void deleteIfExists(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException ignored) {
        }
    }

    private String docxToHtml(Path filePath) throws IOException {
        try (InputStream inputStream = Files.newInputStream(filePath);
             XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder html = new StringBuilder("<html><body>");
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text == null || text.isBlank()) {
                    continue;
                }
                html.append("<p>").append(escapeHtml(text)).append("</p>");
            }
            html.append("</body></html>");
            return html.toString();
        }
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }

    private void createNotification(String type, String message, Long userId, Notification.Actor actor, boolean fromAdmin) {
        if (userId == null) {
            return;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setType(type);
        notification.setMessage(message);
        notification.setUser(user);
        notification.setActor(actor);
        notification.setFromAdmin(fromAdmin);
        notificationRepository.save(notification);
    }
}
