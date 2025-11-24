package com.example.tracnghiem.controller;

import com.example.tracnghiem.domain.user.User;
import com.example.tracnghiem.dto.exam.ExamAttemptResponse;
import com.example.tracnghiem.dto.exam.ExamInstanceResponse;
import com.example.tracnghiem.repository.ExamSupervisorRepository;
import com.example.tracnghiem.service.ExamAttemptService;
import com.example.tracnghiem.service.ExamInstanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/supervisor")
@PreAuthorize("hasRole('SUPERVISOR')")
public class SupervisorController {

    private final ExamSupervisorRepository examSupervisorRepository;
    private final ExamInstanceService examInstanceService;
    private final ExamAttemptService examAttemptService;

    public SupervisorController(ExamSupervisorRepository examSupervisorRepository,
                                ExamInstanceService examInstanceService,
                                ExamAttemptService examAttemptService) {
        this.examSupervisorRepository = examSupervisorRepository;
        this.examInstanceService = examInstanceService;
        this.examAttemptService = examAttemptService;
    }

    @GetMapping("/exams")
    public ResponseEntity<List<ExamInstanceResponse>> myExams(@AuthenticationPrincipal User supervisor) {
        List<ExamInstanceResponse> responses = examSupervisorRepository.findBySupervisor_Id(supervisor.getId()).stream()
                .map(es -> examInstanceService.getInstanceDto(es.getExamInstance().getId()))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/exams/attempts")
    public ResponseEntity<List<ExamAttemptResponse>> attempts(@AuthenticationPrincipal User supervisor) {
        List<Long> examIds = examSupervisorRepository.findBySupervisor_Id(supervisor.getId()).stream()
                .map(es -> es.getExamInstance().getId())
                .toList();
        List<ExamAttemptResponse> attemptResponses = examIds.stream()
                .flatMap(id -> examAttemptService.getAttemptsForInstance(id).stream())
                .toList();
        return ResponseEntity.ok(attemptResponses);
    }
}

