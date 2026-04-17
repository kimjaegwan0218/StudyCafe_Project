package com.example.pentagon.dto.reservation;

public record ReservationCreateRequest(
        String date,         // YYYY-MM-DD
        String time,         // HH:mm
        int durationHours,   // 1~5
        Long seatId
) {}