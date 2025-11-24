package com.example.tracnghiem.domain.exam;

import com.example.tracnghiem.domain.question.Question;
import com.example.tracnghiem.domain.question.QuestionOption;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exam_answers")
public class ExamAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private ExamAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private QuestionOption selectedOption;

    @Column(name = "fill_answer", columnDefinition = "text")
    private String fillAnswer;

    @Column(name = "is_correct")
    private Boolean correct;

    @Column(name = "answered_at")
    private Instant answeredAt;
}

