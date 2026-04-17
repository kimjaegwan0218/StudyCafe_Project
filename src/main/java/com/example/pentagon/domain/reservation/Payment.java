package com.example.pentagon.domain.reservation;

import com.example.pentagon.domain.enums.PaymentStatus;
import com.example.pentagon.domain.reservation.Reservation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments",
        indexes = {
                @Index(name = "idx_payment_reservation", columnList = "reservation_id"),
                @Index(name = "idx_payment_status", columnList = "status")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    @Column(name = "base_price", nullable = false)
    private Integer basePrice;

    @Column(name = "surcharge_price", nullable = false)
    @Builder.Default
    private Integer surchargePrice = 0;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    // ✅ discounts 더미면 연관관계 대신 컬럼
    @Column(name = "discount_id")
    private Long discountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "toss_payment_key", length = 200)
    private String tossPaymentKey;

    @Column(name = "order_id", length = 100, unique = true)
    private String orderId;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
