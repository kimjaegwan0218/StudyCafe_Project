package com.example.pentagon.dto.reservation;

import lombok.*;

/**
 * 공통api레스폰스共通APIレスポンス
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    /**
     * 성공레스폰스(데이터만)成功レスポンス（データのみ）
     */
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * 성공레스폰스(메세지+데이터)成功レスポンス（メッセージ + データ）
     */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * error레스폰스エラーレスポンス
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
