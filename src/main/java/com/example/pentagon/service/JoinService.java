package com.example.pentagon.service;

import com.example.pentagon.domain.User; // 패키지 경로 주의!
import com.example.pentagon.domain.enums.UserRole;
import com.example.pentagon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JoinService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void join(String email, String password, String name, String phoneNumber) {

        // 1. 중복 검사 (findByUsername 대신 findByEmail 사용해야 함)
        // UserRepository에도 findByEmail을 만들어줘야 해! (3단계 참고)
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // 3. 회원 생성 (변수명 매칭 주의!)
        User user = User.builder()
                .email(email)                 // username -> email
                .passwordHash(encodedPassword) // password -> passwordHash
                .name(name)
                .phone(phoneNumber)           // phoneNumber -> phone
                .role(UserRole.USER)          // Role.USER -> UserRole.USER
                .active(true)                 // 계정 활성화 상태로 생성
                .build();

        // 4. 저장
        userRepository.save(user);
    }
}