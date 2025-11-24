package com.example.tracnghiem.domain.group;

import com.example.tracnghiem.domain.subject.Subject;
import com.example.tracnghiem.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "student_group_subjects")
public class StudentGroupSubject {

    @EmbeddedId
    private StudentGroupSubjectId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("studentGroupId")
    @JoinColumn(name = "student_group_id")
    private StudentGroup studentGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("subjectId")
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    @PrePersist
    public void prePersist() {
        if (assignedAt == null) {
            assignedAt = Instant.now();
        }
    }
}

