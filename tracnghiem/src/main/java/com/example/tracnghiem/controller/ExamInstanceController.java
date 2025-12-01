package com.example.tracnghiem.controller;

import com.example.tracnghiem.domain.user.User;
import com.example.tracnghiem.dto.exam.CreateExamInstanceRequest;
import com.example.tracnghiem.dto.exam.ExamInstanceResponse;
import com.example.tracnghiem.dto.exam.UpdateExamInstanceRequest;
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

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<ExamInstanceResponse>> listAll() {
        return ResponseEntity.ok(examInstanceService.getAllInstances());
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

    @GetMapping("/my/all")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ExamInstanceResponse>> getAllMyExams(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(examInstanceService.getAllInstancesForStudent(user.getId()));
    }

    @PostMapping("/{id}/supervisors")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ExamInstanceResponse> assignSupervisors(
            @PathVariable Long id,
            @RequestBody @Valid List<CreateExamInstanceRequest.SupervisorAssignment> supervisors) {
        return ResponseEntity.ok(examInstanceService.assignSupervisorsToInstance(id, supervisors));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ExamInstanceResponse> updateInstance(
            @PathVariable Long id,
            @RequestBody @Valid UpdateExamInstanceRequest request) {
        return ResponseEntity.ok(examInstanceService.updateInstance(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Void> deleteInstance(@PathVariable Long id) {
        examInstanceService.deleteInstance(id);
        return ResponseEntity.ok().build();
    }
}

