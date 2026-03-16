package com.project.app.config;

import com.project.app.entity.User;
import com.project.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InitializerConfig implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password:admin123}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        bootstrapAdmin();
    }

    private void bootstrapAdmin() {
        if (adminEmail == null || adminEmail.isEmpty()) {
            System.out.println("⚠️ ADMIN_EMAIL not found in application.properties. Skipping admin bootstrap.");
            return;
        }

        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setUsername("Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setIsAdmin(true);
            admin.setIsActive(true);
            admin.setPlan("Pro");

            userRepository.save(admin);
            System.out.println("✅ Admin user created: " + adminEmail);
        } else {
            System.out.println("ℹ️ Admin user already exists: " + adminEmail);
        }
    }
}
