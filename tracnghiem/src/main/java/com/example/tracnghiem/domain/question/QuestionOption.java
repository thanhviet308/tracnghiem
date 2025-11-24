package com.example.tracnghiem.domain.question;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "question_options")
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "option_order")
    private Integer optionOrder;
}

