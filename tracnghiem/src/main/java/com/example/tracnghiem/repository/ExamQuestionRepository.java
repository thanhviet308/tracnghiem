package com.example.tracnghiem.repository;

import com.example.tracnghiem.domain.exam.ExamQuestion;
import com.example.tracnghiem.domain.exam.ExamQuestionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, ExamQuestionId> {
    List<ExamQuestion> findByExamInstance_IdOrderByQuestionOrderAsc(Long examInstanceId);
    boolean existsByQuestion_Id(Long questionId);
    
    // Tìm tất cả exam instances chứa câu hỏi này
    @Query("SELECT DISTINCT eq.examInstance.id FROM ExamQuestion eq WHERE eq.question.id = :questionId")
    List<Long> findExamInstanceIdsByQuestionId(@Param("questionId") Long questionId);
    
    void deleteByExamInstance_Id(Long examInstanceId);
}

