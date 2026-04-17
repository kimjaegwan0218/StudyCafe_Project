package com.example.pentagon.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping({"", "/"})
    public String adminRoot() {
        return "redirect:/admin/mypage";
    }

    @GetMapping("/top")
    public String top() {
        return "admin/admin-top";
    }

    @GetMapping("/news")
    public String news() {
        return "admin/admin-news";
    }

    @GetMapping("/booking")
    public String booking() {
        return "admin/admin-booking";
    }

    @GetMapping("/subscription")
    public String subscription() {
        return "admin/admin-subscription";
    }

    @GetMapping("/service")
    public String service() {
        return "admin/admin-service";
    }

    @GetMapping("/community")
    public String community() {
        return "admin/admin-community";
    }

    @GetMapping("/mypage")
    public String mypage() {
        return "admin/admin-mypage";
    }

    @GetMapping("/login")
    public String login() {
        return "admin/admin-login";
    }
}
