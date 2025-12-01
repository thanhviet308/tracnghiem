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
@Table(name = "nhom_mon_hoc")
public class StudentGroupSubject {

    @EmbeddedId
    private StudentGroupSubjectId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("studentGroupId")
    @JoinColumn(name = "ma_nhom")
    private StudentGroup studentGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("subjectId")
    @JoinColumn(name = "ma_mon")
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_giao_vien")
    private User teacher;

    @Column(name = "ngay_phan_cong", nullable = false)
    private Instant assignedAt;

    @PrePersist
    public void prePersist() {
        if (assignedAt == null) {
            assignedAt = Instant.now();
        }
    }
}

