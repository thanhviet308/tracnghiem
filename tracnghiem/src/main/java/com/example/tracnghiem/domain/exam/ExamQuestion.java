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
@Table(name = "cau_hoi_ky_thi")
public class ExamQuestion {

    @EmbeddedId
    private ExamQuestionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("examInstanceId")
    @JoinColumn(name = "ma_ky_thi")
    private ExamInstance examInstance;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionId")
    @JoinColumn(name = "ma_cau_hoi")
    private Question question;

    @Column(name = "thu_tu")
    private Integer questionOrder;
}
