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

    @Column(name = "ma_ky_thi")
    private Long examInstanceId;

    @Column(name = "ma_cau_hoi")
    private Long questionId;
}

