package com.example.tracnghiem.dto.supervisor;

import java.util.List;
import java.util.Map;

public record SupervisorStatisticsResponse(
        int totalSessions,
        int scheduledSessions,
        int ongoingSessions,
        int completedSessions,
        int totalStudents,
        int completedAttempts,
        int totalViolations,
        Map<String, Integer> violationsByType,
        List<SessionSummary> recentSessions,
        List<StudentViolationDetail> studentViolations
) {
    public record SessionSummary(
            Long examInstanceId,
            String examName,
            String subjectName,
            String studentGroupName,
            String startTime,
            String endTime,
            String status,
            int totalStudents,
            int completedStudents,
            int violationsCount
    ) {}
    
    public record StudentViolationDetail(
            Long studentId,
            String studentName,
            Long examInstanceId,
            String examName,
            String subjectName,
            String studentGroupName,
            int totalViolations,
            Map<String, Integer> violationsByType,
            String lastViolationTime
    ) {}
}

