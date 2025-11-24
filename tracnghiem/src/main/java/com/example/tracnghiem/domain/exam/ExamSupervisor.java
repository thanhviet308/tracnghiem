package com.example.tracnghiem.domain.exam;

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
@Table(name = "exam_supervisors")
public class ExamSupervisor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_instance_id", nullable = false)
    private ExamInstance examInstance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id", nullable = false)
    private User supervisor;

    @Column(name = "room_number")
    private String roomNumber;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    @PrePersist
    public void prePersist() {
        if (assignedAt == null) {
            assignedAt = Instant.now();
        }
    }
}

