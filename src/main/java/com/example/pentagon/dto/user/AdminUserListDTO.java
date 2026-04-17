package com.example.pentagon.dto.user;


import com.example.pentagon.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminUserListDTO {

    private Long userId;
    private String name;
    private String email;
    private String phone;
    private boolean active;
    private UserRole role;

}
