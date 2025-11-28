package com.example.tracnghiem.service;

import com.example.tracnghiem.domain.user.User;
import com.example.tracnghiem.domain.user.UserRole;
import com.example.tracnghiem.dto.auth.LoginRequest;
import com.example.tracnghiem.dto.auth.RegisterRequest;
import com.example.tracnghiem.dto.auth.TokenResponse;
import com.example.tracnghiem.exception.BadRequestException;
import com.example.tracnghiem.repository.UserRepository;
import com.example.tracnghiem.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public TokenResponse login(LoginRequest request) {
        // Normalize email to lowercase for consistency
        String normalizedEmail = request.email().toLowerCase().trim();
        log.info("Login attempt: {}", normalizedEmail);

        // Manually authenticate to avoid any misconfiguration with
        // AuthenticationManager
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("Email hoặc mật khẩu không chính xác"));
        log.info("User found: {} ({})", user.getId(), user.getEmail());

        String rawPassword = request.password().trim();
        boolean match = passwordEncoder.matches(rawPassword, user.getPassword());
        log.info("Password match: {}", match);
        if (!match) {
            throw new BadRequestException("Email hoặc mật khẩu không chính xác");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new TokenResponse(accessToken, refreshToken, jwtService.getAccessTokenExpiration(),
                user.getRole(), user.getId(), user.getFullName());
    }

    public TokenResponse refresh(String refreshToken) {
        String email = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));
        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new BadRequestException("Expired refresh token");
        }
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        return new TokenResponse(newAccessToken, newRefreshToken, jwtService.getAccessTokenExpiration(),
                user.getRole(), user.getId(), user.getFullName());
    }

    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already exists");
        }
        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.STUDENT) // Only allow STUDENT registration
                .active(true)
                .build();
        user = userRepository.save(user);
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new TokenResponse(accessToken, refreshToken, jwtService.getAccessTokenExpiration(),
                user.getRole(), user.getId(), user.getFullName());
    }
}
