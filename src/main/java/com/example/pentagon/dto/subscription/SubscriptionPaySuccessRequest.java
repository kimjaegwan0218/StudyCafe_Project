package com.example.pentagon.dto.subscription;

public record SubscriptionPaySuccessRequest(
        int subType,
        String couponCode // 지금은 미사용, 확장용
) {}