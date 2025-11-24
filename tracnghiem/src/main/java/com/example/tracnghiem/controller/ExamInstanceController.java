package com.example.tracnghiem.controller;

import com.example.tracnghiem.domain.user.User;
import com.example.tracnghiem.dto.exam.CreateExamInstanceRequest;
import com.example.tracnghiem.dto.exam.ExamInstanceResponse;
import com.example.tracnghiem.service.ExamInstanceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam-instances")
public class ExamInstanceController {

    private final ExamInstanceService examInstanceService;

    public ExamInstanceController(ExamInstanceService examInstanceService) {
        this.examInstanceService = examInstanceService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ExamInstanceResponse> createInstance(@RequestBody @Valid CreateExamInstanceRequest request) {
        return ResponseEntity.ok(examInstanceService.createInstance(request));
    }

    @GetMapping("/group/{groupId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','SUPERVISOR')")
    public ResponseEntity<List<ExamInstanceResponse>> getByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(examInstanceService.getInstancesForGroup(groupId));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ExamInstanceResponse>> getMyExams(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(examInstanceService.getUpcomingInstancesForStudent(user.getId()));
    }
}

