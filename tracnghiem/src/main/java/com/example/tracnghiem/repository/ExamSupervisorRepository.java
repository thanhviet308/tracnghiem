package com.example.tracnghiem.repository;

import com.example.tracnghiem.domain.exam.ExamSupervisor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExamSupervisorRepository extends JpaRepository<ExamSupervisor, Long> {
    List<ExamSupervisor> findBySupervisor_Id(Long supervisorId);

    List<ExamSupervisor> findByExamInstance_Id(Long examInstanceId);
    
    @Query("SELECT es FROM ExamSupervisor es JOIN FETCH es.examInstance WHERE es.supervisor.id = :supervisorId")
    List<ExamSupervisor> findBySupervisor_IdWithExamInstance(@Param("supervisorId") Long supervisorId);
}

