package com.example.pentagon.controller.admin;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminWhoamiController {

    @GetMapping("/whoami")
    public Map<String, Object> whoami(Authentication auth) {
        return Map.of(
                "authenticated", auth != null && auth.isAuthenticated(),
                "name", auth != null ? auth.getName() : null,
                "authorities", auth != null ? auth.getAuthorities() : null,
                "class", auth != null ? auth.getClass().getName() : null
        );
    }
}
