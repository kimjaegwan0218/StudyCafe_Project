package com.example.pentagon.domain.enums;

public enum PaymentStatus {
    PENDING,
    READY,
    COMPLETED,
    FAILED,
    CANCELLED,
    REFUNDED,
    EXPIRED,

    // ✅ 레거시 호환(기존 데이터/클라이언트가 PAID를 보낼 수 있으니 남겨둠)
    PAID;

    public boolean isPaid() {
        return this == COMPLETED || this == PAID;
    }

    public static PaymentStatus from(String raw) {
        if (raw == null) throw new IllegalArgumentException("status is null");
        String s = raw.trim().toUpperCase();

        return switch (s) {
            case "PAID", "SUCCESS" -> COMPLETED;     // ✅ 들어오면 COMPLETED로 정규화
            case "COMPLETED" -> COMPLETED;
            case "PENDING" -> PENDING;
            case "READY" -> READY;
            case "FAILED" -> FAILED;
            case "CANCELLED", "CANCELED" -> CANCELLED;
            case "REFUNDED" -> REFUNDED;
            case "EXPIRED" -> EXPIRED;
            default -> PaymentStatus.valueOf(s);
        };
    }
}
