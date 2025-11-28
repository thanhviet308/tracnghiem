package com.example.tracnghiem.repository;

import com.example.tracnghiem.domain.exam.ExamTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExamTemplateRepository extends JpaRepository<ExamTemplate, Long> {
    List<ExamTemplate> findBySubject_Id(Long subjectId);
    
    @Query("SELECT DISTINCT t FROM ExamTemplate t " +
           "LEFT JOIN FETCH t.subject")
    List<ExamTemplate> findAllWithSubject();
    
    @Query("SELECT t FROM ExamTemplate t " +
           "LEFT JOIN FETCH t.subject " +
           "LEFT JOIN FETCH t.structures s " +
           "LEFT JOIN FETCH s.chapter " +
           "WHERE t.id = :id")
    java.util.Optional<ExamTemplate> findByIdWithRelations(Long id);
}

