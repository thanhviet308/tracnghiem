package com.example.tracnghiem.domain.question;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lua_chon")
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_cau_hoi", nullable = false)
    private Question question;

    @Column(name = "noi_dung", columnDefinition = "text", nullable = false)
    private String content;

    @Column(name = "dap_an_dung", nullable = false)
    private boolean correct;

    @Column(name = "thu_tu")
    private Integer optionOrder;
}

