package com.example.tracnghiem.repository;

import com.example.tracnghiem.domain.question.Passage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PassageRepository extends JpaRepository<Passage, Long> {
    List<Passage> findByChapter_Id(Long chapterId);
}

