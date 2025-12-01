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
@Table(name = "dap_an_bai_lam")
public class ExamAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bai_lam", nullable = false)
    private ExamAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_cau_hoi", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_lua_chon")
    private QuestionOption selectedOption;

    @Column(name = "dap_an_dien", columnDefinition = "text")
    private String fillAnswer;

    @Column(name = "dap_an_dung")
    private Boolean correct;

    @Column(name = "thoi_gian_tra_loi")
    private Instant answeredAt;
}

