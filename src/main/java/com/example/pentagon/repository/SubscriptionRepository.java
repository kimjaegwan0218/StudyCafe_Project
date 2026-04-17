package com.example.pentagon.repository;

import com.example.pentagon.domain.enums.PaymentStatus;
import com.example.pentagon.domain.reservation.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 구독repositoryサブスクリプションリポジトリ
 *
 * 배치장소配置場所: repository/SubscriptionRepository.java
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    /**
     * 코드로 구독을 검색コードでサブスクリプションを検索
     */
    Optional<Subscription> findByCode(String code);

    /**
     * 유저아이디로 유효한 구독을 검색ユーザーIDで有効なサブスクリプションを検索
     * active = 1 그리고 현제일자가 유효기간내かつ 現在日時が有効期間内
     */
    @Query("SELECT s FROM Subscription s " +
            "WHERE s.user.id = :userId " +
            "AND s.active = 1 " +
            "AND (s.startAt IS NULL OR s.startAt <= :now) " +
            "AND (s.endAt IS NULL OR s.endAt >= :now)")
    Optional<Subscription> findActiveByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * 코드로 유효한 구독을 검색コードで有効なサブスクリプションを検索
     */
    @Query("SELECT s FROM Subscription s " +
            "WHERE s.code = :code " +
            "AND s.active = 1 " +
            "AND (s.startAt IS NULL OR s.startAt <= :now) " +
            "AND (s.endAt IS NULL OR s.endAt >= :now)")
    Optional<Subscription> findActiveByCode(@Param("code") String code, @Param("now") LocalDateTime now);

    /**
     * 주문 아이디로 구독을 검색注文IDでサブスクリプションを検索
     * 결제확인시 사용決済確認時に使用
     */
    Optional<Subscription> findByOrderId(String orderId);

    boolean existsByUserIdAndActiveTrue(Long userId);

    // active=1 이고, endAt이 현재보다 뒤(유효)인 최신 구독 1건
    Optional<Subscription> findTopByUser_IdAndActiveAndEndAtAfterOrderByEndAtDesc(
            Long userId, Integer active, LocalDateTime now
    );

    @Query("""
        select s from Subscription s
        where s.user.id = :userId
          and s.active = 1
          and s.paymentStatus = :status
          and s.endAt > CURRENT_TIMESTAMP
        order by s.endAt desc
    """)
    Optional<Subscription> findLatestActiveByUserId(
            @Param("userId") Long userId,
            @Param("status") PaymentStatus status
    );

    @Query(value = """
        SELECT *
        FROM subscriptions
        WHERE user_id = :userId
          AND active = 1
          AND payment_status = 'COMPLETED'
        ORDER BY end_at DESC
        LIMIT 1
    """, nativeQuery = true)
    Optional<Subscription> findLatestCompletedActive(@Param("userId") Long userId);

    Optional<Subscription> findTopByUser_IdAndActiveAndPaymentStatusOrderByEndAtDesc(
            Long userId,
            Integer active,
            PaymentStatus paymentStatus
    );

    Optional<Subscription> findTopByUser_IdOrderByEndAtDesc(Long userId);
}
