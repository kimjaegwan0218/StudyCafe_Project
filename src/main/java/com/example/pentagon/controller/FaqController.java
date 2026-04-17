package com.example.pentagon.controller;

import com.example.pentagon.domain.support.Faq;
import com.example.pentagon.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class FaqController {

    private final FaqRepository faqRepository;

    @GetMapping("/faq")
    public String faq(Model model) {
        // 1. DB에서 모든 FAQ 가져오기
        List<Faq> faqList = faqRepository.findAll();

        // 2. HTML로 데이터 전달 ("list" 라는 이름으로)
        model.addAttribute("list", faqList);

        return "faq";
    }
}