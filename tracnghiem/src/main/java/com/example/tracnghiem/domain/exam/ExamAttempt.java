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
@Table(name = "bai_lam")
public class ExamAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_ky_thi", nullable = false)
    private ExamInstance examInstance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_sinh_vien", nullable = false)
    private User student;

    @Column(name = "thoi_gian_bat_dau")
    private Instant startedAt;

    @Column(name = "thoi_gian_nop_bai")
    private Instant submittedAt;

    @Column(name = "diem_so")
    private Integer score;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 20)
    private ExamAttemptStatus status = ExamAttemptStatus.NOT_STARTED;
}

