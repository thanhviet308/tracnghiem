package com.example.tracnghiem.controller;

import com.example.tracnghiem.domain.question.QuestionType;
import com.example.tracnghiem.domain.user.User;
import com.example.tracnghiem.dto.question.BulkCreateQuestionResponse;
import com.example.tracnghiem.dto.question.CreateQuestionRequest;
import com.example.tracnghiem.dto.question.QuestionFilterRequest;
import com.example.tracnghiem.dto.question.QuestionResponse;
import com.example.tracnghiem.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<QuestionResponse>> filterQuestions(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Long createdBy,
            @RequestParam(required = false) Boolean hasPassage,
            @RequestParam(required = false) QuestionType questionType
    ) {
        QuestionFilterRequest filter = new QuestionFilterRequest(subjectId, chapterId, difficulty, createdBy, hasPassage, questionType);
        return ResponseEntity.ok(questionService.filterQuestions(filter));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<QuestionResponse> createQuestion(@AuthenticationPrincipal User user,
                                                           @RequestBody @Valid CreateQuestionRequest request) {
        return ResponseEntity.ok(questionService.createQuestion(request, user.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<QuestionResponse> updateQuestion(@PathVariable Long id,
                                                           @AuthenticationPrincipal User user,
                                                           @RequestBody @Valid CreateQuestionRequest request) {
        return ResponseEntity.ok(questionService.updateQuestion(id, request, user.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<BulkCreateQuestionResponse> bulkCreateQuestions(@AuthenticationPrincipal User user,
                                                                      @RequestBody @Valid List<CreateQuestionRequest> requests) {
        return ResponseEntity.ok(questionService.bulkCreateQuestions(requests, user.getId()));
    }
}

