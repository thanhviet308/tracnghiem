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
    @Column(name = "student_group_id")
    private Long studentGroupId;

    @Column(name = "student_id")
    private Long studentId;
}

