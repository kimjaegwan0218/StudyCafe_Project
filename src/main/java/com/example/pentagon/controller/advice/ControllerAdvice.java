package com.example.pentagon.controller.advice;

import com.example.pentagon.config.MyUserDetails;
import com.example.pentagon.domain.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;

public class ControllerAdvice {

    // 모든 페이지(HTML)에 "user"라는 이름으로 로그인 정보를 자동으로 넣어줌!
    @ModelAttribute("user")
    public User getCurrentUser(@AuthenticationPrincipal MyUserDetails myUser) {
        if (myUser == null) {
            return null; // 로그인 안 했으면 null
        }
        return myUser.getUser(); // 로그인 했으면 그 유저 정보 반환
    }
}

