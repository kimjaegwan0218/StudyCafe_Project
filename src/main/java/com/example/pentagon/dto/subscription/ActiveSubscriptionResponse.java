package com.example.pentagon.dto.subscription;

import java.time.LocalDateTime;

public record ActiveSubscriptionResponse(
        boolean active,
        Integer subType,          // 7/30/365 (일수)
        LocalDateTime startAt,
        Integer price,
        String planName,          // "프리미엄 플랜" 등
        Long remainingDays        // 남은 일 수
) {}
