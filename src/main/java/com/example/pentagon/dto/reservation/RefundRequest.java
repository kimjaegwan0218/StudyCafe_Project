package com.example.pentagon.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 환불리퀘스트払い戻しリクエスト
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    private Long paymentId;
    private String cancelReason;   // 캔슬이유キャンセル理由
}
