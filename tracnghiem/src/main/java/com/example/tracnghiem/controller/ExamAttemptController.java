package com.example.tracnghiem.controller;

import com.example.tracnghiem.domain.user.User;
import com.example.tracnghiem.dto.exam.*;
import com.example.tracnghiem.service.ExamAttemptService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam-attempts")
public class ExamAttemptController {

    private final ExamAttemptService examAttemptService;

    public ExamAttemptController(ExamAttemptService examAttemptService) {
        this.examAttemptService = examAttemptService;
    }

    @PostMapping("/{examInstanceId}/start")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StartAttemptResponse> start(@PathVariable Long examInstanceId,
                                                      @AuthenticationPrincipal User student) {
        return ResponseEntity.ok(examAttemptService.startAttempt(examInstanceId, student));
    }

    @PostMapping("/{attemptId}/answers")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ExamAttemptResponse> answer(@PathVariable Long attemptId,
                                                      @AuthenticationPrincipal User student,
                                                      @RequestBody @Valid AnswerQuestionRequest request) {
        return ResponseEntity.ok(examAttemptService.answerQuestion(attemptId, student, request));
    }

    @PostMapping("/{attemptId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SubmitAttemptResponse> submit(@PathVariable Long attemptId,
                                                        @AuthenticationPrincipal User student) {
        return ResponseEntity.ok(examAttemptService.submitAttempt(attemptId, student));
    }

    @GetMapping("/exam/{examInstanceId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','SUPERVISOR')")
    public ResponseEntity<List<ExamAttemptResponse>> attemptsByExam(@PathVariable Long examInstanceId) {
        return ResponseEntity.ok(examAttemptService.getAttemptsForInstance(examInstanceId));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ExamAttemptResponse>> myHistory(@AuthenticationPrincipal User student) {
        return ResponseEntity.ok(examAttemptService.getStudentHistory(student.getId()));
    }

    @GetMapping("/{attemptId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExamAttemptDetailResponse> detail(@PathVariable Long attemptId,
                                                            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(examAttemptService.getAttemptDetail(attemptId, user));
    }
}

