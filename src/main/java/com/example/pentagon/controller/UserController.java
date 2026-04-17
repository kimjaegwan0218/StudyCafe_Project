package com.example.pentagon.controller;

import com.example.pentagon.config.MyUserDetails;
import com.example.pentagon.dto.community.CommunityUserDTO;
import com.example.pentagon.service.ComUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final ComUserService userService;

    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal MyUserDetails userDetails, Model model) {
        Long userId = userDetails.getUser().getId();
        CommunityUserDTO dto = userService.getUser(userId);
        model.addAttribute("user", dto);
        return "mypage";
    }

    @PostMapping("/modify")
    public String modify(@AuthenticationPrincipal MyUserDetails userDetails,
                         @RequestParam String name,
                         @RequestParam String phone) {

        Long userId = userDetails.getUser().getId();
        userService.updateUserProfile(userId, name, phone); // ✅ email은 여기서 건드리지 않음
        return "redirect:/user/mypage";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@AuthenticationPrincipal MyUserDetails userDetails,
                             HttpServletRequest request,
                             HttpServletResponse response) throws Exception {
        Long userId = userDetails.getUser().getId();
        userService.deleteUser(userId); // ✅ soft delete
        new SecurityContextLogoutHandler().logout(request, response, null);
        return "redirect:/login";
    }
}
