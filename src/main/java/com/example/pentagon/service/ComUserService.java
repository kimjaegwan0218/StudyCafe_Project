package com.example.pentagon.service;

import com.example.pentagon.dto.community.CommunityUserDTO;

public interface ComUserService {
    CommunityUserDTO getUser(Long id);
    void deactivateUser(Long userId);
    void updateUserProfile(Long userId, String name, String phone);
    void deleteUser(Long userId);
}
