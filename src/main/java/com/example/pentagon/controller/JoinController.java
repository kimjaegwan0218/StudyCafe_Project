package com.example.pentagon.controller;

import com.example.pentagon.service.JoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class JoinController {

    private final JoinService joinService;

    @GetMapping("/join")
    public String joinForm() {
        return "join";
    }

    @PostMapping("/join")
    public String join(@RequestParam String username, // HTML의 name="username" (이메일)
                       @RequestParam String password,
                       @RequestParam String name,
                       @RequestParam String phoneNumber) {
        try {
            // username(화면값)을 email(서비스 파라미터) 자리에 넣음
            joinService.join(username, password, name, phoneNumber);
            return "redirect:/login";
        } catch (Exception e) {
            return "redirect:/join?error=true";
        }
    }
}