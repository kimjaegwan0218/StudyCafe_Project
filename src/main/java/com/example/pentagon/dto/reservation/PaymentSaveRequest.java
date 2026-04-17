package com.example.pentagon.dto.reservation;

// 프론트가 결제 결과를 보내주면 DB에 "저장만" 하는 용도
public record PaymentSaveRequest(
        String status, // "PAID" | "FAILED" | "CANCELLED" | "REFUNDED" ...
        String paidAt  // optional: "2026-01-12T10:30:00" (없거나 파싱 실패면 서버 now)
) {}