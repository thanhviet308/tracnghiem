package com.example.tracnghiem.domain.group;

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
public class ClassStudentId implements Serializable {
    @Column(name = "ma_nhom")
    private Long studentGroupId;

    @Column(name = "ma_sinh_vien")
    private Long studentId;
}

