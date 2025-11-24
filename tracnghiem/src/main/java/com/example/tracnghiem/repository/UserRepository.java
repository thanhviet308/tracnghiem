package com.example.tracnghiem.repository;

import com.example.tracnghiem.domain.user.User;
import com.example.tracnghiem.domain.user.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findByRole(UserRole role);

    boolean existsByEmail(String email);
}

