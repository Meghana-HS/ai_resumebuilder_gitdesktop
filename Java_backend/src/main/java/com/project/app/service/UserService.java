package com.project.app.service;

import com.project.app.dto.UserProfileDto;
import com.project.app.entity.Notification;
import com.project.app.entity.User;
import com.project.app.repository.ApiMetricRepository;
import com.project.app.repository.AtsScanRepository;
import com.project.app.repository.DownloadRepository;
import com.project.app.repository.NotificationRepository;
import com.project.app.repository.PageViewRepository;
import com.project.app.repository.PaymentRepository;
import com.project.app.repository.ResumeProfileRepository;
import com.project.app.repository.SubscriptionRepository;
import com.project.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private DownloadRepository downloadRepository;

    @Autowired
    private AtsScanRepository atsScanRepository;

    @Autowired
    private ResumeProfileRepository resumeProfileRepository;

    @Autowired
    private PageViewRepository pageViewRepository;

    @Autowired
    private ApiMetricRepository apiMetricRepository;

    public UserProfileDto getProfile(Long userId) {
        return convertToDto(getUserEntity(userId));
    }

    public UserProfileDto updateProfile(Long userId, UserProfileDto profileDto) {
        User user = getUserEntity(userId);

        if (profileDto.getUsername() != null) user.setUsername(profileDto.getUsername());
        if (profileDto.getEmail() != null && !profileDto.getEmail().equalsIgnoreCase(user.getEmail())) {
            ensureEmailAvailable(profileDto.getEmail());
            user.setEmail(profileDto.getEmail());
        }
        if (profileDto.getFullName() != null) user.setFullName(profileDto.getFullName());
        if (profileDto.getPhone() != null) user.setPhone(profileDto.getPhone());
        if (profileDto.getLocation() != null) user.setLocation(profileDto.getLocation());
        if (profileDto.getBio() != null) user.setBio(profileDto.getBio());
        if (profileDto.getGithub() != null) user.setGithub(profileDto.getGithub());
        if (profileDto.getLinkedin() != null) user.setLinkedin(profileDto.getLinkedin());

        return convertToDto(userRepository.save(user));
    }

    public List<UserProfileDto> getAllUsers() {
        return userRepository.findAll().stream().map(this::convertToDto).toList();
    }

    public UserProfileDto updateUser(Long userId, UserProfileDto profileDto, Long actorUserId) {
        User user = getUserEntity(userId);

        if (profileDto.getUsername() != null && !profileDto.getUsername().isBlank()) user.setUsername(profileDto.getUsername());
        if (profileDto.getEmail() != null && !profileDto.getEmail().equalsIgnoreCase(user.getEmail())) {
            ensureEmailAvailable(profileDto.getEmail());
            user.setEmail(profileDto.getEmail());
        }
        if (profileDto.getIsAdmin() != null) user.setIsAdmin(profileDto.getIsAdmin());
        if (profileDto.getIsActive() != null) user.setIsActive(profileDto.getIsActive());
        if (profileDto.getPlan() != null) user.setPlan(profileDto.getPlan());
        if (profileDto.getCreatedAt() != null) user.setCreatedAt(profileDto.getCreatedAt());

        User updatedUser = userRepository.save(user);

        if (profileDto.getIsActive() != null) {
            Notification userNotification = new Notification();
            userNotification.setType("ACCOUNT_STATUS");
            userNotification.setMessage("Your account was " + (updatedUser.getIsActive() ? "activated" : "deactivated") + " by admin");
            userNotification.setUser(updatedUser);
            userNotification.setActor(Notification.Actor.SYSTEM);
            notificationRepository.save(userNotification);

            Notification adminNotification = new Notification();
            adminNotification.setType("USER_STATUS");
            adminNotification.setMessage(updatedUser.getUsername() + " was " + (updatedUser.getIsActive() ? "activated" : "deactivated"));
            adminNotification.setUser(getUserEntity(actorUserId));
            adminNotification.setActor(Notification.Actor.USER);
            adminNotification.setFromAdmin(true);
            notificationRepository.save(adminNotification);
        }

        return convertToDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Long userId, Long actorUserId) {
        User user = getUserEntity(userId);
        User actor = getUserEntity(actorUserId);

        Notification adminNotification = new Notification();
        adminNotification.setType("USER_DELETED");
        adminNotification.setMessage(user.getUsername() + " account was deleted");
        adminNotification.setUser(actor);
        adminNotification.setActor(Notification.Actor.USER);
        adminNotification.setFromAdmin(true);
        notificationRepository.save(adminNotification);

        paymentRepository.deleteByUserId(userId);
        subscriptionRepository.deleteByUserId(userId);
        downloadRepository.deleteByUserId(userId);
        atsScanRepository.deleteAll(atsScanRepository.findByUserId(userId));
        resumeProfileRepository.deleteAll(resumeProfileRepository.findByUser(user));
        pageViewRepository.deleteByUserId(userId);
        apiMetricRepository.deleteByUserId(userId);
        notificationRepository.deleteByUserId(userId);
        userRepository.delete(user);
    }

    public UserProfileDto getUserName(Long userId) {
        User user = getUserEntity(userId);
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        return dto;
    }

    public UserProfileDto requestAdminAccess(Long userId) {
        User user = getUserEntity(userId);
        if (Boolean.TRUE.equals(user.getIsAdmin())) {
            throw new IllegalArgumentException("You are already an admin");
        }
        if (user.getAdminRequestStatus() == User.AdminRequestStatus.PENDING) {
            throw new IllegalArgumentException("Admin request is already pending");
        }

        user.setAdminRequestStatus(User.AdminRequestStatus.PENDING);
        User updatedUser = userRepository.save(user);

        Notification userNotification = new Notification();
        userNotification.setType("ADMIN_REQUEST_USER");
        userNotification.setMessage("Your request for admin access has been submitted");
        userNotification.setUser(user);
        userNotification.setActor(Notification.Actor.SYSTEM);
        notificationRepository.save(userNotification);

        userRepository.findAll().stream()
            .filter(candidate -> Boolean.TRUE.equals(candidate.getIsAdmin()))
            .findFirst()
            .ifPresent(admin -> {
                Notification adminNotification = new Notification();
                adminNotification.setType("ADMIN_REQUEST");
                adminNotification.setMessage((user.getUsername() != null ? user.getUsername() : user.getEmail()) + " has requested for admin access");
                adminNotification.setUser(admin);
                adminNotification.setActor(Notification.Actor.USER);
                notificationRepository.save(adminNotification);
            });

        return convertToDto(updatedUser);
    }

    public UserProfileDto approveAdminRequest(Long userId, Long actorUserId) {
        User user = getUserEntity(userId);
        if (user.getAdminRequestStatus() != User.AdminRequestStatus.PENDING) {
            throw new IllegalArgumentException("No pending admin request for this user");
        }

        user.setIsAdmin(true);
        user.setAdminRequestStatus(User.AdminRequestStatus.APPROVED);
        User updatedUser = userRepository.save(user);

        User admin = getUserEntity(actorUserId);
        String adminName = admin.getUsername() != null ? admin.getUsername() : "Admin";

        Notification userNotification = new Notification();
        userNotification.setType("ROLE_UPDATE");
        userNotification.setMessage("Your admin access request has been approved by " + adminName);
        userNotification.setUser(updatedUser);
        userNotification.setActor(Notification.Actor.SYSTEM);
        notificationRepository.save(userNotification);

        Notification adminNotification = new Notification();
        adminNotification.setType("ROLE_APPROVED_ADMIN");
        adminNotification.setMessage("You approved " + displayName(updatedUser) + "'s admin access request");
        adminNotification.setUser(admin);
        adminNotification.setActor(Notification.Actor.USER);
        adminNotification.setFromAdmin(true);
        notificationRepository.save(adminNotification);

        return convertToDto(updatedUser);
    }

    public UserProfileDto rejectAdminRequest(Long userId, Long actorUserId) {
        User user = getUserEntity(userId);
        if (user.getAdminRequestStatus() != User.AdminRequestStatus.PENDING) {
            throw new IllegalArgumentException("No pending admin request for this user");
        }

        user.setAdminRequestStatus(User.AdminRequestStatus.REJECTED);
        User updatedUser = userRepository.save(user);

        User admin = getUserEntity(actorUserId);
        String adminName = admin.getUsername() != null ? admin.getUsername() : "Admin";

        Notification userNotification = new Notification();
        userNotification.setType("ROLE_UPDATE");
        userNotification.setMessage("Your admin access request was rejected by " + adminName);
        userNotification.setUser(updatedUser);
        userNotification.setActor(Notification.Actor.SYSTEM);
        notificationRepository.save(userNotification);

        Notification adminNotification = new Notification();
        adminNotification.setType("ROLE_REJECTED_ADMIN");
        adminNotification.setMessage("You rejected " + displayName(updatedUser) + "'s admin access request");
        adminNotification.setUser(admin);
        adminNotification.setActor(Notification.Actor.USER);
        adminNotification.setFromAdmin(true);
        notificationRepository.save(adminNotification);

        return convertToDto(updatedUser);
    }

    private User getUserEntity(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private void ensureEmailAvailable(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DataIntegrityViolationException("Email already exists");
        }
    }

    private String displayName(User user) {
        return user.getUsername() != null && !user.getUsername().isBlank() ? user.getUsername() : user.getEmail();
    }

    private UserProfileDto convertToDto(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setLocation(user.getLocation());
        dto.setBio(user.getBio());
        dto.setGithub(user.getGithub());
        dto.setLinkedin(user.getLinkedin());
        dto.setPlan(user.getPlan());
        dto.setIsAdmin(user.getIsAdmin());
        dto.setAdminRequestStatus(user.getAdminRequestStatus().name().toLowerCase());
        dto.setIsActive(user.getIsActive());
        dto.setLastLogin(user.getLastLogin());
        dto.setProfileViews(user.getProfileViews());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
