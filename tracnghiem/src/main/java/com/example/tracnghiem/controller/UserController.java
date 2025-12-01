package com.example.tracnghiem.controller;

import com.example.tracnghiem.domain.user.User;
import com.example.tracnghiem.domain.user.UserRole;
import com.example.tracnghiem.dto.user.CreateUserRequest;
import com.example.tracnghiem.dto.user.UpdateUserRequest;
import com.example.tracnghiem.dto.user.UserResponse;
import com.example.tracnghiem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'SUPERVISOR')")
    public ResponseEntity<List<UserResponse>> listUsers(
            @RequestParam(required = false) UserRole role,
            @AuthenticationPrincipal User currentUser) {
        if (role != null) {
            // ADMIN, TEACHER, and SUPERVISOR can filter by role
            return ResponseEntity.ok(userService.listUsersByRole(role));
        }
        // Only ADMIN can see all users without role filter
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new org.springframework.security.access.AccessDeniedException("Only ADMIN can view all users");
        }
        return ResponseEntity.ok(userService.listUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'SUPERVISOR')")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.getUser(currentUser.getId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid CreateUserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody @Valid UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

