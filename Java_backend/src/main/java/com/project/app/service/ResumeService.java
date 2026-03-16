package com.project.app.service;

import com.project.app.entity.Resume;
import com.project.app.entity.User;
import com.project.app.repository.ResumeRepository;
import com.project.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ResumeService {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<Resume> getUserResume(Long userId) {
        // Temporarily return first resume - will be fixed when User relationship is restored
        return resumeRepository.findAll().stream().findFirst();
    }

    public byte[] downloadResume(String filename, Long userId) {
        // TODO: Implement file download logic
        // This would read the file from the uploads directory and return as byte array
        throw new RuntimeException("File not found: " + filename);
    }

    public byte[] generatePdfFromHtml(String html) {
        // TODO: Implement PDF generation using Puppeteer or similar
        // For now, return a mock PDF
        return "<html><body>Mock PDF content</body></html>".getBytes();
    }

    public Resume saveResume(Resume resume) {
        return resumeRepository.save(resume);
    }

    public void deleteResume(Long resumeId, Long userId) {
        Optional<Resume> resumeOpt = resumeRepository.findById(resumeId);
        if (resumeOpt.isEmpty()) {
            throw new RuntimeException("Resume not found");
        }
        // TODO: Add user validation when User relationship is restored
        resumeRepository.deleteById(resumeId);
    }
}
