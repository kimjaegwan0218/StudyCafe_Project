package com.example.pentagon.repository;

import com.example.pentagon.domain.support.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqRepository extends JpaRepository<Faq, Long> {
    // 필요한 경우 카테고리별 조회 등을 추가할 수 있어
    // List<Faq> findByCategory(String category);
}
