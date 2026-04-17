package com.example.pentagon.controller;

import com.example.pentagon.config.MyUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

public class MainController {

    @GetMapping("/")
    public String home(@AuthenticationPrincipal MyUserDetails myUser, Model model) {
        if (myUser != null) {
            System.out.println("로그인한 사람 이름: " + myUser.getUser().getName());
            System.out.println("로그인한 사람 폰번: " + myUser.getUser().getPhone());

            model.addAttribute("userName", myUser.getUser().getName());
        }
        return "index";
    }
}