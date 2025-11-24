package com.example.tracnghiem.repository;

import com.example.tracnghiem.domain.subject.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findBySubject_Id(Long subjectId);
}

