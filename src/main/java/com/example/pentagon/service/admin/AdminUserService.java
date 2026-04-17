package com.example.pentagon.service.admin;


import com.example.pentagon.dto.user.AdminUserDetailDTO;
import com.example.pentagon.dto.user.AdminUserListDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AdminUserService {


    AdminUserDetailDTO getUserDetail(Long id);

    Page<AdminUserListDTO> getAllUsers(int page, int size);

    Page<AdminUserListDTO> searchUsers(String keyword, int page, int size);

    void setActive(Long userId, boolean active);
}
