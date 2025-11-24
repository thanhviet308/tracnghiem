package com.example.tracnghiem.dto.statistics;

import java.util.List;
import java.util.Map;

public record ExamStatisticsResponse(
        double averageScore,
        int totalAttempts,
        int completedAttempts,
        double maxScore,
        Map<String, Long> scoreDistribution,
        List<QuestionAccuracy> questionAccuracy,
        List<ChapterAccuracy> chapterAccuracy
) {
    public record QuestionAccuracy(Long questionId, String content, double correctRate) {
    }

    public record ChapterAccuracy(Long chapterId, String chapterName, double correctRate) {
    }
}

