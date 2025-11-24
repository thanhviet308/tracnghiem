package com.example.tracnghiem.domain.exam;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class ExamQuestionId implements Serializable {

    @Column(name = "exam_instance_id")
    private Long examInstanceId;

    @Column(name = "question_id")
    private Long questionId;
}

