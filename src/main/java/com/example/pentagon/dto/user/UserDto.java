package com.example.pentagon.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;


public class UserDto {

    // 회원정보 수정 데이터를 받는 내부 클래스 (static class로 만들어야 해)
    @Data
    @NoArgsConstructor
    public static class UpdateCurrent {
        private String name;
    }

}