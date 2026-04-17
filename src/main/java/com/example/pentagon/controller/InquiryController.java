package com.example.pentagon.controller;

import com.example.pentagon.config.MyUserDetails;
import com.example.pentagon.domain.support.Inquiry;
import com.example.pentagon.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    // 1. 문의글 목록
    @GetMapping("/list")
    public String list(Model model,
                       @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Inquiry> list = inquiryService.boardList(pageable);
        int nowPage = list.getPageable().getPageNumber() + 1;
        int startPage = Math.max(nowPage - 9, 1);
        int endPage = Math.min(nowPage + 10, list.getTotalPages());

        model.addAttribute("list", list);
        model.addAttribute("nowPage", nowPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "list";
    }

    // 2. 글쓰기 화면
    @GetMapping("/write")
    public String writeForm() {
        return "inquiry_write";
    }

    // 3. 글쓰기 저장
    @PostMapping("/write")
    public String write(@AuthenticationPrincipal MyUserDetails myUser,
                        @RequestParam String title,
                        @RequestParam String content) {
        if (myUser != null) {
            inquiryService.write(myUser.getUser(), title, content);
        }
        return "redirect:/mypage";
    }

    // 4. 상세보기
    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        Inquiry inquiry = inquiryService.getInquiry(id);
        model.addAttribute("inquiry", inquiry);
        return "inquiry_view";
    }

    // 5. 수정 화면
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Inquiry inquiry = inquiryService.getInquiry(id);
        model.addAttribute("inquiry", inquiry);
        return "inquiry_edit";
    }

    // 6. [수정 처리] "success" 제거! -> 페이지 유지하며 리다이렉트
    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @AuthenticationPrincipal MyUserDetails myUser,
                       @RequestParam String title,
                       @RequestParam String content,
                       @RequestParam(defaultValue = "0") int page) { // 👈 페이지 번호 받기

        if (myUser != null) {
            inquiryService.update(id, myUser.getUser(), title, content);
        }

        // 👇 "success" 대신 원래 페이지로 돌아가되, 팝업은 열어둠!
        return "redirect:/mypage?historyOpen=true&page=" + page;
    }

    // 7. [삭제 처리]
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal MyUserDetails myUser,
                         @RequestParam(defaultValue = "0") int page) {
        if (myUser != null) {
            inquiryService.delete(id, myUser.getUser());
        }
        return "redirect:/mypage?historyOpen=true&page=" + page;
    }
}