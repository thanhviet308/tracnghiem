package com.example.tracnghiem.repository;

import com.example.tracnghiem.domain.exam.ExamInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ExamInstanceRepository extends JpaRepository<ExamInstance, Long> {
    List<ExamInstance> findByStudentGroup_Id(Long groupId);

    List<ExamInstance> findByTemplate_Subject_Id(Long subjectId);

    List<ExamInstance> findByStudentGroup_IdAndStartTimeBeforeAndEndTimeAfter(Long groupId, Instant start, Instant end);
}

