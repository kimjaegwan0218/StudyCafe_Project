package com.example.pentagon.dto.user;


import com.example.pentagon.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class AdminUserDetailDTO {

    private Long userId;
    private String name;
    private String email;
    private String phone;
    private boolean active;
    private UserRole role;              // ✅ enum으로 변경
    private boolean subscriptionActive; // ✅ boolean으로 통일

}
