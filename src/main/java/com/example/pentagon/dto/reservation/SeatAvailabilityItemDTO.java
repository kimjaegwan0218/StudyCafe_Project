package com.example.pentagon.dto.reservation;

import com.example.pentagon.domain.enums.SeatType;

public record SeatAvailabilityItemDTO(
        Long seatId,
        SeatType type,
        int surchargePrice,
        String status // "open" | "occupied"
) {}