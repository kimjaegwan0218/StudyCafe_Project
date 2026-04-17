package com.example.pentagon.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TossConfirmRequest {
    private String paymentKey;  // 토스가 발행하는 결제 키Tossが発行する決済キー
    private String orderId;   // 여기서 생성한 주문아이디こちらで生成した注文ID
    private Integer amount;   // 결제금액決済金額
}
