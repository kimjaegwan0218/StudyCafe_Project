package com.example.pentagon.service.admin;


import com.example.pentagon.dto.user.AdminUserDetailDTO;

public interface AdminSubscriptionsService {

    AdminUserDetailDTO getAdminUserDetail(Long userId);

}
