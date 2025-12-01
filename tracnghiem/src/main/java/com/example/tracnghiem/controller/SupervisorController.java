package com.example.tracnghiem.controller;

import com.example.tracnghiem.domain.exam.ExamSupervisor;
import com.example.tracnghiem.domain.user.User;
import com.example.tracnghiem.dto.exam.ExamAttemptResponse;
import com.example.tracnghiem.dto.exam.ExamInstanceResponse;
import com.example.tracnghiem.dto.supervisor.SupervisorStatisticsResponse;
import com.example.tracnghiem.repository.ExamSupervisorRepository;
import com.example.tracnghiem.service.ExamAttemptService;
import com.example.tracnghiem.service.ExamInstanceService;
import com.example.tracnghiem.service.SupervisorService;
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
    private final SupervisorService supervisorService;

    public SupervisorController(ExamSupervisorRepository examSupervisorRepository,
                                ExamInstanceService examInstanceService,
                                ExamAttemptService examAttemptService,
                                SupervisorService supervisorService) {
        this.examSupervisorRepository = examSupervisorRepository;
        this.examInstanceService = examInstanceService;
        this.examAttemptService = examAttemptService;
        this.supervisorService = supervisorService;
    }

    @GetMapping("/exams")
    public ResponseEntity<List<ExamInstanceResponse>> myExams(@AuthenticationPrincipal User supervisor) {
        // Use JOIN FETCH to eagerly load examInstance to avoid LazyInitializationException
        List<ExamSupervisor> assignments = examSupervisorRepository.findBySupervisor_IdWithExamInstance(supervisor.getId());
        
        // Debug logging
        System.out.println("[SupervisorController] Supervisor ID: " + supervisor.getId());
        System.out.println("[SupervisorController] Found " + assignments.size() + " assignments");
        
        List<ExamInstanceResponse> responses = assignments.stream()
                .map(es -> {
                    try {
                        Long examInstanceId = es.getExamInstance().getId();
                        System.out.println("[SupervisorController] Processing ExamInstance ID: " + examInstanceId);
                        return examInstanceService.getInstanceDto(examInstanceId);
                    } catch (Exception e) {
                        System.err.println("[SupervisorController] Error getting instance: " + e.getMessage());
                        e.printStackTrace();
                        throw e;
                    }
                })
                .toList();
        
        System.out.println("[SupervisorController] Returning " + responses.size() + " exam instances");
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

    @GetMapping("/statistics")
    public ResponseEntity<SupervisorStatisticsResponse> statistics(@AuthenticationPrincipal User supervisor) {
        return ResponseEntity.ok(supervisorService.getStatistics(supervisor));
    }
}

