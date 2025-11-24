package com.example.tracnghiem.repository;

import com.example.tracnghiem.domain.exam.ExamSupervisor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamSupervisorRepository extends JpaRepository<ExamSupervisor, Long> {
    List<ExamSupervisor> findBySupervisor_Id(Long supervisorId);

    List<ExamSupervisor> findByExamInstance_Id(Long examInstanceId);
}

