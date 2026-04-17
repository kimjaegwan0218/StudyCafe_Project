package com.example.pentagon.repository;

import com.example.pentagon.domain.enums.PaymentStatus;
import com.example.pentagon.domain.reservation.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * 예약 ID로 결제 조회 (1:1)
     * Reservation 엔티티의 필드가 reservationId 인 구조에 맞춤
     */
    Optional<Payment> findByReservation_ReservationId(Long reservationId);

    /**
     * 주문 ID로 결제 조회 (Toss 연계)
     */
    Optional<Payment> findByOrderId(String orderId);

    /**
     * Toss paymentKey로 결제 조회
     */
    Optional<Payment> findByTossPaymentKey(String tossPaymentKey);

    /**
     * 결제 상태로 조회
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * 유저 ID로 결제 이력 조회
     * - Reservation에 User 엔티티가 없고 userId(Long)만 있으므로 p.reservation.userId 로 접근
     */
    @Query("SELECT p FROM Payment p WHERE p.reservation.userId = :userId ORDER BY p.createdAt DESC")
    List<Payment> findByUserId(@Param("userId") Long userId);

    /**
     * 기간 만료된 결제 조회 (배치/정리용)
     * - status = PENDING 이면서
     * - reservation.paymentExpiresAt 이 현재 시각보다 과거인 것
     * - paymentExpiresAt null 방어 포함
     */
    @Query("""
           SELECT p FROM Payment p
           WHERE p.status = :status
             AND p.reservation.paymentExpiresAt IS NOT NULL
             AND p.reservation.paymentExpiresAt < CURRENT_TIMESTAMP
           """)
    List<Payment> findExpiredPayments(@Param("status") PaymentStatus status);

    /**
     * (테스트/관리용) 특정 시각 기준 만료 결제 조회
     */
    @Query("""
           SELECT p FROM Payment p
           WHERE p.status = :status
             AND p.reservation.paymentExpiresAt IS NOT NULL
             AND p.reservation.paymentExpiresAt < :now
           """)
    List<Payment> findExpiredPaymentsAt(@Param("status") PaymentStatus status,
                                        @Param("now") LocalDateTime now);
}
