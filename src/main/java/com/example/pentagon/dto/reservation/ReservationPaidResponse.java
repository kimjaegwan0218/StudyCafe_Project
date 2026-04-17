package com.example.pentagon.dto.reservation;

public record ReservationPaidResponse(
        Long reservationId,
        java.time.LocalDateTime paidAt
) {}
