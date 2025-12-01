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
public class StudentGroupSubjectId implements Serializable {

    @Column(name = "ma_nhom")
    private Long studentGroupId;

    @Column(name = "ma_mon")
    private Long subjectId;
}

