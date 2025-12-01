package com.example.tracnghiem.service;

import com.example.tracnghiem.domain.exam.ExamAttemptStatus;
import com.example.tracnghiem.domain.exam.ExamSupervisor;
import com.example.tracnghiem.domain.user.User;
import com.example.tracnghiem.dto.exam.ExamAttemptResponse;
import com.example.tracnghiem.dto.exam.ExamInstanceResponse;
import com.example.tracnghiem.dto.exam.ViolationResponse;
import com.example.tracnghiem.dto.supervisor.SupervisorStatisticsResponse;
import com.example.tracnghiem.repository.ExamSupervisorRepository;
import com.example.tracnghiem.repository.StudentGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupervisorService {

    private final ExamSupervisorRepository examSupervisorRepository;
    private final ExamInstanceService examInstanceService;
    private final ExamAttemptService examAttemptService;
    private final ViolationService violationService;
    private final StudentGroupRepository studentGroupRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public SupervisorService(ExamSupervisorRepository examSupervisorRepository,
                            ExamInstanceService examInstanceService,
                            ExamAttemptService examAttemptService,
                            ViolationService violationService,
                            StudentGroupRepository studentGroupRepository) {
        this.examSupervisorRepository = examSupervisorRepository;
        this.examInstanceService = examInstanceService;
        this.examAttemptService = examAttemptService;
        this.violationService = violationService;
        this.studentGroupRepository = studentGroupRepository;
    }

    public SupervisorStatisticsResponse getStatistics(User supervisor) {
        // Get all exam instances assigned to this supervisor
        List<ExamSupervisor> assignments = examSupervisorRepository.findBySupervisor_IdWithExamInstance(supervisor.getId());
        List<Long> examInstanceIds = assignments.stream()
                .map(es -> es.getExamInstance().getId())
                .toList();

        if (examInstanceIds.isEmpty()) {
            return new SupervisorStatisticsResponse(
                    0, 0, 0, 0, 0, 0, 0,
                    Map.of(),
                    List.of(),
                    List.of()
            );
        }

        // Get all exam instances
        List<ExamInstanceResponse> instances = examInstanceIds.stream()
                .map(examInstanceService::getInstanceDto)
                .toList();

        // Calculate status counts
        Instant now = Instant.now();
        int scheduledSessions = 0;
        int ongoingSessions = 0;
        int completedSessions = 0;

        for (ExamInstanceResponse instance : instances) {
            Instant startTime = instance.startTime();
            Instant endTime = instance.endTime();
            if (now.isBefore(startTime)) {
                scheduledSessions++;
            } else if (now.isAfter(endTime)) {
                completedSessions++;
            } else {
                ongoingSessions++;
            }
        }

        // Get all attempts for these exams
        List<ExamAttemptResponse> allAttempts = examInstanceIds.stream()
                .flatMap(id -> examAttemptService.getAttemptsForInstance(id).stream())
                .toList();

        // Calculate student statistics
        Set<Long> uniqueStudents = allAttempts.stream()
                .map(ExamAttemptResponse::studentId)
                .collect(Collectors.toSet());
        int totalStudents = uniqueStudents.size();

        int completedAttempts = (int) allAttempts.stream()
                .filter(a -> a.status() == ExamAttemptStatus.SUBMITTED || a.status() == ExamAttemptStatus.GRADED)
                .count();

        // Get all violations
        List<ViolationResponse> allViolations = examInstanceIds.stream()
                .flatMap(id -> violationService.getViolationsForExam(id).stream())
                .toList();

        int totalViolations = allViolations.size();

        // Group violations by type
        Map<String, Integer> violationsByType = allViolations.stream()
                .collect(Collectors.groupingBy(
                        ViolationResponse::violationType,
                        Collectors.summingInt(ViolationResponse::violationCount)
                ));

        // Get recent sessions (last 10 completed or ongoing)
        List<SupervisorStatisticsResponse.SessionSummary> recentSessions = instances.stream()
                .sorted((a, b) -> b.startTime().compareTo(a.startTime()))
                .limit(10)
                .map(instance -> {
                    List<ExamAttemptResponse> instanceAttempts = examAttemptService.getAttemptsForInstance(instance.id());
                    Set<Long> instanceStudents = instanceAttempts.stream()
                            .map(ExamAttemptResponse::studentId)
                            .collect(Collectors.toSet());
                    int instanceCompleted = (int) instanceAttempts.stream()
                            .filter(a -> a.status() == ExamAttemptStatus.SUBMITTED || a.status() == ExamAttemptStatus.GRADED)
                            .count();
                    List<ViolationResponse> instanceViolations = violationService.getViolationsForExam(instance.id());
                    int instanceViolationsCount = instanceViolations.stream()
                            .mapToInt(ViolationResponse::violationCount)
                            .sum();

                    String status;
                    if (now.isBefore(instance.startTime())) {
                        status = "SCHEDULED";
                    } else if (now.isAfter(instance.endTime())) {
                        status = "COMPLETED";
                    } else {
                        status = "ONGOING";
                    }

                    String studentGroupName = studentGroupRepository.findById(instance.studentGroupId())
                            .map(g -> g.getName())
                            .orElse("Chưa xác định");

                    return new SupervisorStatisticsResponse.SessionSummary(
                            instance.id(),
                            instance.name(),
                            instance.subjectName() != null ? instance.subjectName() : "Chưa xác định",
                            studentGroupName,
                            DATE_TIME_FORMATTER.format(instance.startTime()),
                            DATE_TIME_FORMATTER.format(instance.endTime()),
                            status,
                            instanceStudents.size(),
                            instanceCompleted,
                            instanceViolationsCount
                    );
                })
                .toList();

        // Get detailed student violations - group by student and exam instance
        // First, get all attempts to map attemptId to examInstanceId
        Map<Long, Long> attemptToExamInstance = new HashMap<>();
        for (Long examInstanceId : examInstanceIds) {
            List<ExamAttemptResponse> attempts = examAttemptService.getAttemptsForInstance(examInstanceId);
            for (ExamAttemptResponse attempt : attempts) {
                attemptToExamInstance.put(attempt.attemptId(), examInstanceId);
            }
        }

        // Group violations by student and exam instance
        Map<String, List<ViolationResponse>> violationsByStudentAndExam = new HashMap<>();
        for (ViolationResponse violation : allViolations) {
            Long examInstanceId = attemptToExamInstance.get(violation.attemptId());
            if (examInstanceId != null) {
                String key = violation.studentId() + "-" + examInstanceId;
                violationsByStudentAndExam.computeIfAbsent(key, k -> new ArrayList<>()).add(violation);
            }
        }

        List<SupervisorStatisticsResponse.StudentViolationDetail> studentViolations = new ArrayList<>();
        for (Map.Entry<String, List<ViolationResponse>> entry : violationsByStudentAndExam.entrySet()) {
            List<ViolationResponse> studentViols = entry.getValue();
            if (studentViols.isEmpty()) continue;
            
            ViolationResponse firstViolation = studentViols.get(0);
            Long studentId = firstViolation.studentId();
            Long examInstanceId = attemptToExamInstance.get(firstViolation.attemptId());
            
            ExamInstanceResponse examInstance = instances.stream()
                    .filter(inst -> inst.id().equals(examInstanceId))
                    .findFirst()
                    .orElse(null);
            
            if (examInstance != null) {
                String studentName = firstViolation.studentName();
                String studentGroupName = studentGroupRepository.findById(examInstance.studentGroupId())
                        .map(g -> g.getName())
                        .orElse("Chưa xác định");
                
                int totalViols = studentViols.stream()
                        .mapToInt(ViolationResponse::violationCount)
                        .sum();
                
                Map<String, Integer> violsByType = studentViols.stream()
                        .collect(Collectors.groupingBy(
                                ViolationResponse::violationType,
                                Collectors.summingInt(ViolationResponse::violationCount)
                        ));
                
                Instant lastViolTime = studentViols.stream()
                        .map(ViolationResponse::lastOccurredAt)
                        .max(Instant::compareTo)
                        .orElse(Instant.now());
                
                studentViolations.add(new SupervisorStatisticsResponse.StudentViolationDetail(
                        studentId,
                        studentName,
                        examInstance.id(),
                        examInstance.name(),
                        examInstance.subjectName() != null ? examInstance.subjectName() : "Chưa xác định",
                        studentGroupName,
                        totalViols,
                        violsByType,
                        DATE_TIME_FORMATTER.format(lastViolTime)
                ));
            }
        }
        
        // Sort by total violations descending
        studentViolations.sort((a, b) -> Integer.compare(b.totalViolations(), a.totalViolations()));

        return new SupervisorStatisticsResponse(
                instances.size(),
                scheduledSessions,
                ongoingSessions,
                completedSessions,
                totalStudents,
                completedAttempts,
                totalViolations,
                violationsByType,
                recentSessions,
                studentViolations
        );
    }
}

