package com.example.pentagon.dto.subscription;

import java.time.LocalDateTime;


public record SubscriptionPaySuccessResponse(
        String code,
        long userId,
        int subType,
        int price,
        boolean active,
        LocalDateTime startAt
) {}
