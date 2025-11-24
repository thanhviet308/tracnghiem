package com.example.tracnghiem.repository;

import com.example.tracnghiem.domain.question.QuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, Long> {
    List<QuestionAnswer> findByQuestion_Id(Long questionId);
}

