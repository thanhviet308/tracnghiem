package com.example.tracnghiem.controller;

import com.example.tracnghiem.dto.statistics.ExamStatisticsResponse;
import com.example.tracnghiem.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@PreAuthorize("hasAnyRole('ADMIN','TEACHER','SUPERVISOR')")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/exam/{examInstanceId}")
    public ResponseEntity<ExamStatisticsResponse> examStatistics(@PathVariable Long examInstanceId) {
        return ResponseEntity.ok(statisticsService.getExamStatistics(examInstanceId));
    }
}

