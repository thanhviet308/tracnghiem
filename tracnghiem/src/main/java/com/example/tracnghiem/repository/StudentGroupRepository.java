package com.example.tracnghiem.repository;

import com.example.tracnghiem.domain.group.StudentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {
    Optional<StudentGroup> findByName(String name);
}

