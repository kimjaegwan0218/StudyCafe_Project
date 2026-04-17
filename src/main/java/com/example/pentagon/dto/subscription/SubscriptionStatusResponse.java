package com.example.pentagon.dto.subscription;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record SubscriptionStatusResponse(
        boolean active,
        String code,
        String subscriptionType,   // WEEK_7 / MONTH_30 / YEAR_365
        LocalDateTime startAt,
        LocalDateTime endAt
) {}
