package com.example.tracnghiem.domain.question;

import com.example.tracnghiem.domain.subject.Chapter;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "doan_van")
public class Passage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_chuong", nullable = false)
    private Chapter chapter;

    @Column(name = "noi_dung", columnDefinition = "text", nullable = false)
    private String content;

    @Column(name = "ngay_tao", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
