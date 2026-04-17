package com.example.pentagon.domain.reservation;

import com.example.pentagon.domain.User;
import com.example.pentagon.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Subscription {

    @Id
    @Column(length = 255)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_subscriptions_user_id"))
    private User user;

    @Column(length = 50)
    private String type;  // WEEK_7, MONTH_30, YEAR_365

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(nullable = false)
    @Builder.Default
    private Integer active = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ★ 결제관련필드추가決済関連フィールド追加

    /** 주문ID注文ID（Toss用용） SUB-yyyyMMddHHmmss-XXXX */
    @Column(name = "order_id", length = 100, unique = true)
    private String orderId;

    /** Toss결제키決済キー */
    @Column(name = "toss_payment_key", length = 200)
    private String tossPaymentKey;

    /** 決済方法결제방식（CARD등等） */
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    /** 決済ステータス결제상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    /** 元料金원래금액 */
    @Column(name = "price")
    private Integer price;

    /** 割引額할인금액（デフォルト0） */
    @Column(name = "discount_amount")
    @Builder.Default
    private Integer discountAmount = 0;

    /** 決済金額결제금액 */
    @Column(name = "total_price")
    private Integer totalPrice;

    /** 決済完了日時결제완료일시 */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /** 決済期限결제기간 */
    @Column(name = "payment_expires_at")
    private LocalDateTime paymentExpiresAt;
}
