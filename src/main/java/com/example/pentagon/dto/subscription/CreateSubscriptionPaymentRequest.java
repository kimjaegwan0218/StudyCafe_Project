package com.example.pentagon.dto.subscription;

import lombok.*;

/**
 * 구독결제작성 리퀘스트サブスクリプション決済作成リクエスト
 *
 * POST /api/subscription-payments
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSubscriptionPaymentRequest {

    /** 유저 아이디ユーザーID */
    private Long userId;

    /** 플랜타입プランタイプ（WEEK_7, MONTH_30, YEAR_365） */
    private String subscriptionType;

    /** 쿠폰코드(옵션)クーポンコード（オプション） */
    private String couponCode;
}
