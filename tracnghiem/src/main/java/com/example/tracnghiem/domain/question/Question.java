package com.example.tracnghiem.domain.question;

import com.example.tracnghiem.domain.subject.Chapter;
import com.example.tracnghiem.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cau_hoi")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_chuong", nullable = false)
    private Chapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_doan_van")
    private Passage passage;

    @Column(name = "noi_dung", columnDefinition = "text", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_cau_hoi", nullable = false)
    private QuestionType questionType;

    @Column(name = "do_kho", length = 20)
    private String difficulty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_tao")
    private User createdBy;

    @Column(name = "ngay_tao", nullable = false)
    private Instant createdAt;

    @Column(name = "trang_thai", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private List<QuestionOption> options = new ArrayList<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionAnswer> answers = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
