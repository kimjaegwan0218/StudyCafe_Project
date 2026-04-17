package com.example.pentagon.dto.subscription;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 구독 결제작성 리스폰스サブスクリプション決済作成レスポンス
 *
 * POST /api/subscription-payments
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSubscriptionPaymentResponse {

    /** 처리됫나안됬나処理成否 */
    private boolean success;

    /** 구독코드サブスクリプションコード（SUB-XXXX） */
    private String subscriptionCode;

    /** 주문아이디注文ID（SUB-yyyyMMddHHmmss-XXXX） */
    private String orderId;

    /** 결제금액決済金額 */
    private Integer amount;

    /** 주문명注文名（例예시: "StudyCafe 30일정액플랙日定額プラン"） */
    private String orderName;

    /** 결제기간決済期限 */
    private LocalDateTime expiresAt;

    /** メッセージ */
    private String message;
}
