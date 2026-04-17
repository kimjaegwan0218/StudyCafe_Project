package com.example.pentagon.domain.reservation;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reservations",
        indexes = {
                @Index(name = "idx_res_seat_start", columnList = "seat_id,start_at"),
                @Index(name = "idx_res_user", columnList = "user_id")
        }
)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;

    // 로그인 파트 분리: User 엔티티 없이 userId만 저장
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt; // 10분 단위

    @Column(name = "duration_hours", nullable = false)
    private int durationHours; // 1~10

    // 결제 기한(= 홀드 만료). "유지" 정책이라 만료돼도 row는 남음
    @Column(name = "payment_expires_at")
    private LocalDateTime paymentExpiresAt;

    // 결제 성공 시각(성공 시에만)
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public boolean isPaid() { return paidAt != null; }

    public Long getReservationId() { return reservationId; }
    public Long getUserId() { return userId; }
    public Seat getSeat() { return seat; }
    public LocalDateTime getStartAt() { return startAt; }
    public int getDurationHours() { return durationHours; }
    public LocalDateTime getPaymentExpiresAt() { return paymentExpiresAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setSeat(Seat seat) { this.seat = seat; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }
    public void setPaymentExpiresAt(LocalDateTime paymentExpiresAt) { this.paymentExpiresAt = paymentExpiresAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}
