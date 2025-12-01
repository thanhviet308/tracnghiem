package com.example.tracnghiem.repository;

import com.example.tracnghiem.domain.exam.ExamStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExamStructureRepository extends JpaRepository<ExamStructure, Long> {
    List<ExamStructure> findByTemplate_Id(Long templateId);
    
    @Query("SELECT s FROM ExamStructure s " +
           "LEFT JOIN FETCH s.chapter " +
           "LEFT JOIN FETCH s.template " +
           "WHERE s.template.id IN :templateIds")
    List<ExamStructure> findByTemplate_IdIn(List<Long> templateIds);
    
    void deleteByTemplate_Id(Long templateId);
}

