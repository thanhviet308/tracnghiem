package com.example.tracnghiem.service;

import com.example.tracnghiem.domain.user.User;
import com.example.tracnghiem.domain.user.UserRole;
import com.example.tracnghiem.dto.user.CreateUserRequest;
import com.example.tracnghiem.dto.user.UpdateUserRequest;
import com.example.tracnghiem.dto.user.UserResponse;
import com.example.tracnghiem.exception.BadRequestException;
import com.example.tracnghiem.exception.ResourceNotFoundException;
import com.example.tracnghiem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<UserResponse> listUsersByRole(UserRole role) {
        return userRepository.findByRole(role).stream().map(this::toResponse).toList();
    }

    public UserResponse getUser(Long id) {
        return toResponse(findEntity(id));
    }

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already exists");
        }
        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .active(request.active())
                .build();
        return toResponse(userRepository.save(user));
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findEntity(id);
        if (!user.getEmail().equalsIgnoreCase(request.email())
                && userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already exists");
        }
        user.setFullName(request.fullName());
        user.setEmail(request.email().toLowerCase());
        user.setRole(request.role());
        user.setActive(request.active());
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }
        return toResponse(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        User user = findEntity(id);
        userRepository.delete(user);
    }

    private User findEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.isActive()
        );
    }
}

