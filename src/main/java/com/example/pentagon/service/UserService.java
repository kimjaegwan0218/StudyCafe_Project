package com.example.pentagon.service;

import com.example.pentagon.domain.User;
import com.example.pentagon.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void updateName(String email, String newName) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setName(newName); // Dirty Checking에 의해 자동으로 DB 반영
    }
}

