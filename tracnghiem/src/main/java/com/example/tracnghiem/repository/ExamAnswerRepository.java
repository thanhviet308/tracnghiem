package com.example.tracnghiem.repository;

import com.example.tracnghiem.domain.exam.ExamAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamAnswerRepository extends JpaRepository<ExamAnswer, Long> {
    Optional<ExamAnswer> findByAttempt_IdAndQuestion_Id(Long attemptId, Long questionId);

    List<ExamAnswer> findByAttempt_Id(Long attemptId);

    List<ExamAnswer> findByAttempt_IdIn(List<Long> attemptIds);
}

