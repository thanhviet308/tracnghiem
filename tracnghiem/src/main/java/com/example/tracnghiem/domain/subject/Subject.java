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
@Table(name = "mon_hoc")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_mon", nullable = false, unique = true)
    private String name;

    @Column(name = "mo_ta", columnDefinition = "text")
    private String description;

    @Column(name = "ngay_tao", nullable = false)
    private Instant createdAt;

    @Column(name = "trang_thai", nullable = false)
    private boolean active = true;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

