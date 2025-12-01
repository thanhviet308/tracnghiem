package com.example.tracnghiem.domain.exam;

import com.example.tracnghiem.domain.subject.Subject;
import com.example.tracnghiem.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "khung_de_thi")
public class ExamTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_mon", nullable = false)
    private Subject subject;

    @Column(name = "ten_de", nullable = false)
    private String name;

    @Column(name = "tong_so_cau", nullable = false)
    private Integer totalQuestions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_tao")
    private User createdBy;

    @Column(name = "ngay_tao", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamStructure> structures = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

