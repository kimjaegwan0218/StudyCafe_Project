package com.example.pentagon.domain.reservation;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * サブスクリプション料金マスタ
 *
 * 配置場所: domain/reservation/SubscriptionPrice.java
 *
 * 各プランの料金を管理
 * - WEEK_7: 7日間プラン
 * - MONTH_30: 30日間プラン
 * - YEAR_365: 365日間プラン
 */
@Entity
@Table(name = "subscription_price")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPrice {

    /** 플랜타입プランタイプ（WEEK_7, MONTH_30, YEAR_365） */
    @Id
    @Column(name = "subscription_type", length = 50)
    private String subscriptionType;

    /** 요금料金（円） */
    @Column(nullable = false)
    private Integer price;

    /** 유효일수有効日数（7, 30, 365） */
    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    /** 유효플래그有効フラグ (1=판매중有効, 0=판매중지無効) */
    @Column(nullable = false)
    @Builder.Default
    private Integer active = 1;

    /** 작성일자作成日時 */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
