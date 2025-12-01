package com.example.tracnghiem.domain.exam;

import com.example.tracnghiem.domain.group.StudentGroup;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ky_thi")
public class ExamInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_khung_de", nullable = false)
    private ExamTemplate template;

    @Column(name = "ten_ky_thi", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nhom", nullable = false)
    private StudentGroup studentGroup;

    @Column(name = "thoi_gian_bat_dau", nullable = false)
    private Instant startTime;

    @Column(name = "thoi_gian_ket_thuc", nullable = false)
    private Instant endTime;

    @Column(name = "thoi_luong_phut", nullable = false)
    private Integer durationMinutes;

    @Column(name = "tron_cau_hoi", nullable = false)
    private boolean shuffleQuestions;

    @Column(name = "tron_dap_an", nullable = false)
    private boolean shuffleOptions;

    @Column(name = "tong_diem", nullable = false)
    private Integer totalMarks; // Tổng điểm của đề thi

    @Column(name = "ngay_tao", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
