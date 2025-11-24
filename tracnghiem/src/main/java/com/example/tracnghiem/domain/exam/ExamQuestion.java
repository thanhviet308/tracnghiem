package com.example.tracnghiem.domain.exam;

import com.example.tracnghiem.domain.question.Question;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exam_questions")
public class ExamQuestion {

    @EmbeddedId
    private ExamQuestionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("examInstanceId")
    @JoinColumn(name = "exam_instance_id")
    private ExamInstance examInstance;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionId")
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(name = "question_order")
    private Integer questionOrder;
}

