package com.example.pentagon.dto.reservation;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReservationResponse {
    private boolean success;
    private Long reservationId;
    private Long paymentId;
    private String orderId; // 토스용주문아이디Toss用の注文ID
    private Integer amount; // 결제금액決済金額
    private String orderName; // 주문명("예시 studycafe 3시간 팩")注文名（例: "StudyCafe 3時間パック"）
    private LocalDateTime expiresAt;  // 결제기간決済期限
    private String message;
}
