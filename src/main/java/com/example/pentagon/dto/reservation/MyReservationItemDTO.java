package com.example.pentagon.dto.reservation;

import java.time.LocalDateTime;

public record MyReservationItemDTO(
        Long reservationId,
        Long seatId,
        String seatType,              // "1인석" / "2인석" / "회의실"
        LocalDateTime startAt,
        LocalDateTime endAt,
        Integer durationHours,
        String paymentState,          // "결제완료" / "결제대기" / "결제만료"
        String useState,              // "이용예정" / "이용중" / "이용완료" / "결제대기" / "예약만료"
        LocalDateTime paidAt,
        LocalDateTime paymentExpiresAt
) {}
