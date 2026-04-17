package com.example.pentagon.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. 정적 리소스(CSS, JS, 이미지)는 보안 검사 안 함 (화면 깨짐 방지)
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                .requestMatchers("/assets/**", "/favicon.ico", "/error");
    }

    // 2. 로그인 및 페이지 접근 권한 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // 복잡한 보안 설정 끄기
                .authorizeHttpRequests(auth -> auth
                        // ✅ 관리자만 접근
                        .requestMatchers("/api/admin/**","/admin/**").hasRole("ADMIN")

                        // 로그인 없이 볼 수 있는 페이지들
                        .requestMatchers("/", "/login", "/join", "/news/**", "/service", "/community", "/booking", "/faq").permitAll()
                        // 나머지는 로그인 필수
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username") // HTML input name은 여전히 "username"
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true) // 성공 시 메인으로
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login") // 로그아웃 후 로그인 페이지로
                        .invalidateHttpSession(true)
                ).exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            String uri = request.getRequestURI();
                            if (uri != null && uri.startsWith("/admin")) {
                                response.sendRedirect("/");     // ✅ USER가 /admin 들어오면 메인으로
                            } else {
                                response.sendError(403);        // 그 외는 기본 403
                            }
                        })
                );;

        return http.build();
    }

    // 3. 비밀번호 암호화 기능
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}