package com.example.pentagon.dto.reservation;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 영수증데이터領収書データ
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptData {
    private String receiptId;
    private Long reservationId;
    private Long paymentId;
    private String userName;
    private Long seatId;
    private String seatName;
    private LocalDateTime startAt;
    private Integer durationHours;
    private Integer basePrice;
    private Integer surchargePrice;
    private Integer discountAmount;
    private Integer totalPrice;
    private LocalDateTime paidAt;
    private String paymentMethod;


}
