package com.example.pentagon.domain.support;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
@Table(name = "faqs")
public class Faq {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category; // 카테고리 (예: "예약", "결제")

    @Column(nullable = false)
    private String question; // 질문

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;   // 답변
}