package com.example.tracnghiem.service;

import com.example.tracnghiem.domain.exam.ExamAnswer;
import com.example.tracnghiem.domain.exam.ExamAttempt;
import com.example.tracnghiem.domain.exam.ExamInstance;
import com.example.tracnghiem.domain.exam.ExamQuestion;
import com.example.tracnghiem.domain.question.Question;
import com.example.tracnghiem.dto.statistics.ExamStatisticsResponse;
import com.example.tracnghiem.repository.ExamAnswerRepository;
import com.example.tracnghiem.repository.ExamAttemptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StatisticsService {

    private final ExamAttemptRepository examAttemptRepository;
    private final ExamAnswerRepository examAnswerRepository;
    private final ExamInstanceService examInstanceService;

    public StatisticsService(ExamAttemptRepository examAttemptRepository,
                             ExamAnswerRepository examAnswerRepository,
                             ExamInstanceService examInstanceService) {
        this.examAttemptRepository = examAttemptRepository;
        this.examAnswerRepository = examAnswerRepository;
        this.examInstanceService = examInstanceService;
    }

    public ExamStatisticsResponse getExamStatistics(Long examInstanceId) {
        ExamInstance examInstance = examInstanceService.getExamInstance(examInstanceId);
        List<ExamAttempt> attempts = examAttemptRepository.findByExamInstance_Id(examInstanceId);
        List<Long> attemptIds = attempts.stream().map(ExamAttempt::getId).toList();
        List<ExamAnswer> answers = attemptIds.isEmpty() ? List.of() : examAnswerRepository.findByAttempt_IdIn(attemptIds);
        List<ExamQuestion> examQuestions = examInstanceService.getExamQuestions(examInstanceId);

        double maxScore = examQuestions.stream().mapToInt(eq -> eq.getQuestion().getMarks()).sum();
        double averageScore = attempts.stream()
                .filter(a -> a.getScore() != null)
                .mapToInt(ExamAttempt::getScore)
                .average()
                .orElse(0);
        int completedAttempts = (int) attempts.stream()
                .filter(a -> a.getSubmittedAt() != null)
                .count();

        Map<String, Long> distribution = buildDistribution(attempts, maxScore);
        List<ExamStatisticsResponse.QuestionAccuracy> questionAccuracy = buildQuestionAccuracy(examQuestions, answers);
        List<ExamStatisticsResponse.ChapterAccuracy> chapterAccuracy = buildChapterAccuracy(examQuestions, questionAccuracy);

        return new ExamStatisticsResponse(
                averageScore,
                attempts.size(),
                completedAttempts,
                maxScore,
                distribution,
                questionAccuracy,
                chapterAccuracy
        );
    }

    private Map<String, Long> buildDistribution(List<ExamAttempt> attempts, double maxScore) {
        if (maxScore == 0) {
            return Map.of("0-25", 0L, "25-50", 0L, "50-75", 0L, "75-100", 0L);
        }
        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put("0-25", 0L);
        distribution.put("25-50", 0L);
        distribution.put("50-75", 0L);
        distribution.put("75-100", 0L);

        attempts.stream()
                .filter(a -> a.getScore() != null)
                .forEach(attempt -> {
                    double percentage = (attempt.getScore() / maxScore) * 100;
                    if (percentage < 25) {
                        distribution.computeIfPresent("0-25", (k, v) -> v + 1);
                    } else if (percentage < 50) {
                        distribution.computeIfPresent("25-50", (k, v) -> v + 1);
                    } else if (percentage < 75) {
                        distribution.computeIfPresent("50-75", (k, v) -> v + 1);
                    } else {
                        distribution.computeIfPresent("75-100", (k, v) -> v + 1);
                    }
                });
        return distribution;
    }

    private List<ExamStatisticsResponse.QuestionAccuracy> buildQuestionAccuracy(List<ExamQuestion> examQuestions, List<ExamAnswer> answers) {
        Map<Long, QuestionStats> stats = new HashMap<>();
        examQuestions.forEach(eq -> stats.put(eq.getQuestion().getId(), new QuestionStats(eq.getQuestion())));

        answers.forEach(answer -> {
            QuestionStats stat = stats.get(answer.getQuestion().getId());
            if (stat != null) {
                stat.incrementTotal();
                if (Boolean.TRUE.equals(answer.getCorrect())) {
                    stat.incrementCorrect();
                }
            }
        });

        return stats.values().stream()
                .map(stat -> new ExamStatisticsResponse.QuestionAccuracy(
                        stat.question.getId(),
                        stat.question.getContent(),
                        stat.total == 0 ? 0 : (double) stat.correct / stat.total
                ))
                .sorted(Comparator.comparing(ExamStatisticsResponse.QuestionAccuracy::questionId))
                .toList();
    }

    private List<ExamStatisticsResponse.ChapterAccuracy> buildChapterAccuracy(List<ExamQuestion> examQuestions,
                                                                              List<ExamStatisticsResponse.QuestionAccuracy> questionAccuracy) {
        Map<Long, ExamStatisticsResponse.QuestionAccuracy> qaMap = questionAccuracy.stream()
                .collect(Collectors.toMap(ExamStatisticsResponse.QuestionAccuracy::questionId, qa -> qa));

        Map<Long, List<Question>> chapterQuestions = examQuestions.stream()
                .collect(Collectors.groupingBy(
                        eq -> eq.getQuestion().getChapter().getId(),
                        Collectors.mapping(ExamQuestion::getQuestion, Collectors.toList())
                ));

        return chapterQuestions.entrySet().stream()
                .map(entry -> {
                    double avg = entry.getValue().stream()
                            .mapToDouble(question -> qaMap.getOrDefault(
                                    question.getId(),
                                    new ExamStatisticsResponse.QuestionAccuracy(question.getId(), question.getContent(), 0)
                            ).correctRate())
                            .average()
                            .orElse(0);
                    String chapterName = entry.getValue().stream()
                            .findFirst()
                            .map(q -> q.getChapter().getName())
                            .orElse("");
                    return new ExamStatisticsResponse.ChapterAccuracy(entry.getKey(), chapterName, avg);
                })
                .toList();
    }

    private static class QuestionStats {
        private final Question question;
        private int correct;
        private int total;

        QuestionStats(Question question) {
            this.question = question;
        }

        void incrementCorrect() {
            correct++;
        }

        void incrementTotal() {
            total++;
        }
    }
}

