package com.example.tracnghiem.repository;

import com.example.tracnghiem.domain.subject.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
}

