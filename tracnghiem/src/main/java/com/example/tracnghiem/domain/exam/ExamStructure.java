package com.example.tracnghiem.domain.exam;

import com.example.tracnghiem.domain.subject.Chapter;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cau_truc_de")
public class ExamStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_khung_de", nullable = false)
    private ExamTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_chuong", nullable = false)
    private Chapter chapter;

    @Column(name = "so_cau", nullable = false)
    private Integer numQuestion;

    @Builder.Default
    @Column(name = "so_cau_co_ban", nullable = false)
    private Integer numBasic = 0; // Số câu hỏi cơ bản

    @Builder.Default
    @Column(name = "so_cau_nang_cao", nullable = false)
    private Integer numAdvanced = 0; // Số câu hỏi nâng cao
}
