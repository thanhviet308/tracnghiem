package com.example.tracnghiem.domain.group;

import com.example.tracnghiem.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lop_sinh_vien")
public class ClassStudent {

    @EmbeddedId
    private ClassStudentId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("studentGroupId")
    @JoinColumn(name = "ma_nhom")
    private StudentGroup studentGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("studentId")
    @JoinColumn(name = "ma_sinh_vien")
    private User student;

}

