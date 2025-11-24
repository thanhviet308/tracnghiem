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
@Table(name = "class_student")
public class ClassStudent {

    @EmbeddedId
    private ClassStudentId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("studentGroupId")
    @JoinColumn(name = "student_group_id")
    private StudentGroup studentGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("studentId")
    @JoinColumn(name = "student_id")
    private User student;

}

