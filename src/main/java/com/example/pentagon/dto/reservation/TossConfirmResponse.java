package com.example.pentagon.dto.reservation;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossConfirmResponse {
    private boolean success;
    private Long paymentId;
    private Long reservationId;
    private String status;
    private Integer totalPrice;
    private String receiptId;   // 영수증아이디領収書ID
    private String receiptUrl;   // 영수증 다운로드 url 領収書ダウンロードURL
    private String message;
}
