package com.example.pentagon.service.admin;


import com.example.pentagon.dto.user.AdminUserDetailDTO;
import com.example.pentagon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
@RequiredArgsConstructor
public class AdminSubscriptionsServiceImpl implements AdminSubscriptionsService {

    private final UserRepository userRepository;

    @Override
    public AdminUserDetailDTO getAdminUserDetail(Long userId) {
        return userRepository.findUserDetailWithActiveSubscription(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

}
