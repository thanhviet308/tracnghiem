package com.example.tracnghiem.controller;

import com.example.tracnghiem.domain.user.User;
import com.example.tracnghiem.dto.exam.CreateExamTemplateRequest;
import com.example.tracnghiem.dto.exam.ExamTemplateResponse;
import com.example.tracnghiem.service.ExamTemplateService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam-templates")
@PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
public class ExamTemplateController {

    private final ExamTemplateService examTemplateService;

    public ExamTemplateController(ExamTemplateService examTemplateService) {
        this.examTemplateService = examTemplateService;
    }

    @PostMapping
    public ResponseEntity<ExamTemplateResponse> createTemplate(@AuthenticationPrincipal User user,
                                                               @RequestBody @Valid CreateExamTemplateRequest request) {
        return ResponseEntity.ok(examTemplateService.createTemplate(request, user.getId()));
    }

    @GetMapping
    public ResponseEntity<List<ExamTemplateResponse>> listTemplates(@RequestParam Long subjectId) {
        return ResponseEntity.ok(examTemplateService.getTemplatesBySubject(subjectId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamTemplateResponse> getTemplate(@PathVariable Long id) {
        return ResponseEntity.ok(examTemplateService.getTemplate(id));
    }
}

