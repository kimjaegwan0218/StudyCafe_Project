package com.example.pentagon.service;

import com.example.pentagon.config.MyUserDetails;
import com.example.pentagon.domain.User;
import com.example.pentagon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service // 👈 이거 꼭 있어야 스프링이 알아봐!
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. DB에서 이메일로 유저 찾기
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("없는 회원입니다: " + email));

        // 2. [핵심] 찾은 유저를 'MyUserDetails' 포장지로 감싸서 던져주기!
        // 그래야 컨트롤러에서 @AuthenticationPrincipal MyUserDetails 로 받을 수 있어.
        return new MyUserDetails(user);
    }
}
