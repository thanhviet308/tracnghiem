package com.example.tracnghiem.domain.subject;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chuong")
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_mon", nullable = false)
    private Subject subject;

    @Column(name = "ten_chuong", nullable = false)
    private String name;

    @Column(name = "mo_ta", columnDefinition = "text")
    private String description;

    @Column(name = "ngay_tao", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

