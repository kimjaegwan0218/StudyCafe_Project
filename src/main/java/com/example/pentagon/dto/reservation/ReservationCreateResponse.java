package com.example.pentagon.dto.reservation;


import com.example.pentagon.domain.enums.SeatType;

import java.time.LocalDateTime;

public record ReservationCreateResponse(
        Long reservationId,
        Long seatId,
        SeatType seatType,
        LocalDateTime startAt,
        int durationHours,
        int basePrice,
        int surchargePrice,
        int totalPrice,
        LocalDateTime paymentExpiresAt,
        Long paymentId
) {}
