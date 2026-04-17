package com.example.pentagon.dto.reservation;

import java.time.LocalDateTime;

public record MyReservationResponse(
        Long id,
        LocalDateTime startAt,
        Integer durationHours,
        Integer seatNumber,
        String status
) {}
