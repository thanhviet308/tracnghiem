package com.example.tracnghiem.repository;

import com.example.tracnghiem.domain.exam.ExamTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamTemplateRepository extends JpaRepository<ExamTemplate, Long> {
    List<ExamTemplate> findBySubject_Id(Long subjectId);
}

