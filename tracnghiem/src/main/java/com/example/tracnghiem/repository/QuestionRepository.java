package com.example.tracnghiem.repository;

import com.example.tracnghiem.domain.question.Question;
import com.example.tracnghiem.domain.question.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("""
            SELECT q FROM Question q
            WHERE (:subjectId IS NULL OR q.chapter.subject.id = :subjectId)
            AND (:chapterId IS NULL OR q.chapter.id = :chapterId)
            AND (:difficulty IS NULL OR q.difficulty = :difficulty)
            AND (:createdBy IS NULL OR q.createdBy.id = :createdBy)
            AND (
                :withPassage IS NULL
                OR (:withPassage = TRUE AND q.passage IS NOT NULL)
                OR (:withPassage = FALSE AND q.passage IS NULL)
            )
            AND (:questionType IS NULL OR q.questionType = :questionType)
            """)
    List<Question> filter(
            @Param("subjectId") Long subjectId,
            @Param("chapterId") Long chapterId,
            @Param("difficulty") String difficulty,
            @Param("createdBy") Long createdBy,
            @Param("withPassage") Boolean withPassage,
            @Param("questionType") QuestionType questionType);

    List<Question> findByChapter_IdAndActiveTrue(Long chapterId);

    long countByChapter_IdAndActiveTrue(Long chapterId);
    
    // Tìm tất cả câu hỏi cùng passage
    List<Question> findByPassage_IdAndActiveTrue(Long passageId);
    
    // Tìm câu hỏi trùng dựa trên content, chapter, passage
    @Query("""
            SELECT q FROM Question q
            WHERE q.chapter.id = :chapterId
            AND q.content = :content
            AND (
                (:passageId IS NULL AND q.passage IS NULL)
                OR (q.passage IS NOT NULL AND q.passage.id = :passageId)
            )
            """)
    List<Question> findDuplicates(
            @Param("chapterId") Long chapterId,
            @Param("content") String content,
            @Param("passageId") Long passageId);
}
