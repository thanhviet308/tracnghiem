package com.example.tracnghiem.domain.exam;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vi_pham")
public class ExamViolation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bai_lam", nullable = false)
    private ExamAttempt attempt;

    @Column(name = "loai_vi_pham", nullable = false, length = 50)
    private String violationType; // TAB_SWITCH, COPY, PASTE, WINDOW_BLUR

    @Column(name = "so_lan_vi_pham", nullable = false)
    @lombok.Builder.Default
    private Integer violationCount = 1;

    @Column(name = "lan_cuoi_xay_ra", nullable = false)
    private Instant lastOccurredAt;

    @Column(name = "ngay_tao", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (lastOccurredAt == null) {
            lastOccurredAt = Instant.now();
        }
    }
}

