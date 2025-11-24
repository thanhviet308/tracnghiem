package com.example.tracnghiem.repository;

import com.example.tracnghiem.domain.exam.ExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {

    Optional<ExamAttempt> findByExamInstance_IdAndStudent_Id(Long examInstanceId, Long studentId);

    List<ExamAttempt> findByExamInstance_Id(Long examInstanceId);

    List<ExamAttempt> findByStudent_Id(Long studentId);
}

