package com.example.tracnghiem.service;

import com.example.tracnghiem.domain.user.User;
import com.example.tracnghiem.dto.auth.LoginRequest;
import com.example.tracnghiem.dto.auth.TokenResponse;
import com.example.tracnghiem.exception.BadRequestException;
import com.example.tracnghiem.repository.UserRepository;
import com.example.tracnghiem.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = (User) authentication.getPrincipal();
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
}

