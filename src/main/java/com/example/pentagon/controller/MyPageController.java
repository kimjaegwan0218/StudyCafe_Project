package com.example.pentagon.controller;

import com.example.pentagon.domain.User;
import com.example.pentagon.domain.support.Inquiry;
import com.example.pentagon.repository.UserRepository;
import com.example.pentagon.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class MyPageController {

    private final InquiryService inquiryService; // 문의 서비스 연결
    private final UserRepository userRepository; // 유저 서비스 연결

    @GetMapping("/mypage")
    public String myPage(Principal principal,
                         Model model,
                         @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        // 1. 로그인 안 되어 있으면 로그인 페이지로 쫓아내기
        if (principal == null) {
            return "redirect:/login";
        }

        // 2. 로그인 ID(이메일)로 유저 정보 찾기
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return "redirect:/login";
        }

        // 3. 화면에 유저 정보 보내기
        model.addAttribute("user", user);

        // 4. 문의 내역 가져오기 (여기서 에러 안 나게 안전장치 추가)
        Page<Inquiry> inquiryPage = inquiryService.getMyInquiries(email, pageable);

        // 페이징 계산 (데이터가 없어도 에러 안 나게 처리)
        int nowPage = 1;
        int startPage = 1;
        int endPage = 1;

        if (inquiryPage != null && !inquiryPage.isEmpty()) {
            nowPage = inquiryPage.getPageable().getPageNumber() + 1;

            // 현재 페이지가 속한 10페이지 블록의 첫 번째 페이지 계산 (예: 1, 11, 21...)
            startPage = ((nowPage - 1) / 10) * 10 + 1;

            // 블록의 마지막 페이지 계산 (시작페이지 + 9), 단 전체 페이지 수를 넘지 않게
            endPage = Math.min(startPage + 10 - 1, inquiryPage.getTotalPages());
        }

        model.addAttribute("inquiries", inquiryPage);
        model.addAttribute("nowPage", nowPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "mypage";
    }
}