package com.project.app.service;

import com.project.app.dto.UserProfileDto;
import com.project.app.entity.User;
import com.project.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserProfileDto getProfile(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();
        return convertToDto(user);
    }

    public UserProfileDto updateProfile(Long userId, UserProfileDto profileDto) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();
        user.setFullName(profileDto.getFullName());
        user.setPhone(profileDto.getPhone());
        user.setLocation(profileDto.getLocation());
        user.setBio(profileDto.getBio());
        user.setGithub(profileDto.getGithub());
        user.setLinkedin(profileDto.getLinkedin());

        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    public Page<UserProfileDto> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::convertToDto);
    }

    public UserProfileDto updateUser(Long userId, UserProfileDto profileDto) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();
        user.setUsername(profileDto.getUsername());
        user.setFullName(profileDto.getFullName());
        user.setPhone(profileDto.getPhone());
        user.setLocation(profileDto.getLocation());
        user.setBio(profileDto.getBio());
        user.setGithub(profileDto.getGithub());
        user.setLinkedin(profileDto.getLinkedin());
        user.setPlan(profileDto.getPlan());
        user.setIsActive(profileDto.getIsActive());

        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(userId);
    }

    public UserProfileDto getUserName(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        return dto;
    }

    public void requestAdminAccess(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();
        user.setAdminRequestStatus(User.AdminRequestStatus.PENDING);
        userRepository.save(user);
    }

    public UserProfileDto approveAdminRequest(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();
        user.setAdminRequestStatus(User.AdminRequestStatus.APPROVED);
        user.setIsAdmin(true);

        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    public UserProfileDto rejectAdminRequest(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();
        user.setAdminRequestStatus(User.AdminRequestStatus.REJECTED);

        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
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
        dto.setIsActive(user.getIsActive());
        dto.setLastLogin(user.getLastLogin());
        dto.setProfileViews(user.getProfileViews());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
