package com.example.pentagon.dto.community;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityUserDTO {

    private Long id;
    private String email;
    private String name;
    private String phone;
    // 필요한 경우 active나 role 추가 가능
}
