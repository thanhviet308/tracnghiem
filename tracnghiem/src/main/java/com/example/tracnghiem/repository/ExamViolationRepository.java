package com.example.tracnghiem.repository;

import com.example.tracnghiem.domain.exam.ExamViolation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamViolationRepository extends JpaRepository<ExamViolation, Long> {
    List<ExamViolation> findByAttempt_Id(Long attemptId);
    
    Optional<ExamViolation> findByAttempt_IdAndViolationType(Long attemptId, String violationType);
    
    List<ExamViolation> findByAttempt_ExamInstance_Id(Long examInstanceId);
}

