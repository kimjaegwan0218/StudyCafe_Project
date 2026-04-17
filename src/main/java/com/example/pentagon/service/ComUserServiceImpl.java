package com.example.pentagon.service;

import com.example.pentagon.domain.User;
import com.example.pentagon.dto.community.CommunityUserDTO;
import com.example.pentagon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ComUserServiceImpl implements ComUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public CommunityUserDTO getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다. id=" + id));

        return CommunityUserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .build();
    }

    @Transactional
    public void updateUserProfile(Long userId, String name, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 유저가 없습니다."));

        user.setName(name);
        user.setPhone(phone);
        // user.setEmail(...) ❌ 금지(로그인 아이디 깨짐)
    }

    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 유저가 없습니다."));

        user.setActive(false); // ✅ MyUserDetails.isEnabled()가 active를 보니까 로그인 차단됨
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        // 1) 로그인 차단
        user.setActive(false);

        // 2) 개인정보 익명화
        user.setName("탈퇴한 사용자");
        user.setPhone(null);

        // 3) 이메일 unique 회피 → 같은 이메일로 재가입 가능해짐
        String newEmail = "deleted_" + user.getId() + "_" + System.currentTimeMillis() + "@deleted.local";
        user.setEmail(newEmail);

        // 4) 비밀번호 무효화(권장)
        user.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
    }
}
