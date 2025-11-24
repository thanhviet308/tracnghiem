package com.example.tracnghiem.repository;

import com.example.tracnghiem.domain.exam.ExamStructure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamStructureRepository extends JpaRepository<ExamStructure, Long> {
    List<ExamStructure> findByTemplate_Id(Long templateId);
}

