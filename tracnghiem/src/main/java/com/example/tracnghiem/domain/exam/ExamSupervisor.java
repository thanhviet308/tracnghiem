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
@Table(name = "giam_thi")
public class ExamSupervisor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_ky_thi", nullable = false)
    private ExamInstance examInstance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_giam_thi", nullable = false)
    private User supervisor;

    @Column(name = "ngay_phan_cong", nullable = false)
    private Instant assignedAt;

    @PrePersist
    public void prePersist() {
        if (assignedAt == null) {
            assignedAt = Instant.now();
        }
    }
}

