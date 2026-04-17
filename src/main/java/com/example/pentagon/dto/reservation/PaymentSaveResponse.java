package com.example.pentagon.dto.reservation;

import java.time.LocalDateTime;

public record PaymentSaveResponse(
        Long reservationId,
        Long paymentId,
        String status,
        LocalDateTime paidAt
) {}