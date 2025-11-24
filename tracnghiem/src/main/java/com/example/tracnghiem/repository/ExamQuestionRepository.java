package com.example.tracnghiem.repository;

import com.example.tracnghiem.domain.exam.ExamQuestion;
import com.example.tracnghiem.domain.exam.ExamQuestionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, ExamQuestionId> {
    List<ExamQuestion> findByExamInstance_IdOrderByQuestionOrderAsc(Long examInstanceId);
}

