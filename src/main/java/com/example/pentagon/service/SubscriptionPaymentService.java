package com.example.pentagon.service;

import com.example.pentagon.domain.User;
import com.example.pentagon.domain.enums.PaymentStatus;
import com.example.pentagon.domain.reservation.Subscription;
import com.example.pentagon.domain.reservation.SubscriptionPrice;
import com.example.pentagon.dto.subscription.CreateSubscriptionPaymentRequest;
import com.example.pentagon.dto.subscription.CreateSubscriptionPaymentResponse;
import com.example.pentagon.dto.reservation.TossConfirmRequest;
import com.example.pentagon.dto.reservation.TossConfirmResponse;
import com.example.pentagon.repository.SubscriptionPriceRepository;
import com.example.pentagon.repository.SubscriptionRepository;
import com.example.pentagon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 구독결제서비스サブスクリプション決済サービス
 *
 * 배치장소配置場所: service/SubscriptionPaymentService.java
 *
 * 구독 구입의 결제처리를 담당サブスクリプション購入の決済処理を担当
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPaymentService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPriceRepository subscriptionPriceRepository;
    private final UserRepository userRepository;
    private final TossPaymentService tossPaymentService;

    // 결제기한(분)決済期限（分）
    private static final int PAYMENT_EXPIRY_MINUTES = 15;

    /**
     * 구독결제를 작성サブスクリプション決済を作成
     *
     * 1. 요금플랜취득料金プラン取得
     * 2. 쿠폰적용(옵션)クーポン適用（オプション）
     * 3. Subscription작성作成（paymentStatus=PENDING, active=0）
     * 4. orderId생성生成
     */
    @Transactional
    public CreateSubscriptionPaymentResponse createSubscriptionPayment(CreateSubscriptionPaymentRequest request) {
        log.info("サブスク결제작성개시: userId={}, type={}", request.getUserId(), request.getSubscriptionType());

        try {
            // === 1. 유저취득ユーザー取得 ===
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("유저가없어요"));

            // === 2. 요금플랜취득料金プラン取得 ===
            SubscriptionPrice priceInfo = subscriptionPriceRepository.findBySubscriptionType(request.getSubscriptionType())
                    .orElseThrow(() -> new RuntimeException("무효한플랜타입이에요: " + request.getSubscriptionType()));

            if (priceInfo.getActive() == null || priceInfo.getActive() != 1) {
                throw new RuntimeException("이플랜은현재이용못해요");
            }

            int price = priceInfo.getPrice();
            int durationDays = priceInfo.getDurationDays();

            // === 3. 쿠폰적용(옵션)クーポン適用（オプション） ===
            int discountAmount = 0;
            if (request.getCouponCode() != null && !request.getCouponCode().isEmpty()) {
                discountAmount = getDiscountAmount(request.getCouponCode());
            }

            // === 4. 합계금액계산合計金額計算 ===
            int totalPrice = price - discountAmount;
            totalPrice = Math.max(0, totalPrice);

            log.info("요금계산: price={}, discount={}, total={}", price, discountAmount, totalPrice);

            // === 5. orderId생성 ===
            String orderId = generateSubscriptionOrderId();
            String subscriptionCode = generateSubscriptionCode();

            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(PAYMENT_EXPIRY_MINUTES);

            // === 6. Subscription작성 ===
            Subscription subscription = Subscription.builder()
                    .code(subscriptionCode)
                    .user(user)
                    .type(request.getSubscriptionType())
                    .active(0)  // 결제완료까지 무효決済完了まで無効
                    .orderId(orderId)
                    .paymentStatus(PaymentStatus.PENDING)
                    .price(price)
                    .discountAmount(discountAmount)
                    .totalPrice(totalPrice)
                    .paymentExpiresAt(expiresAt)
                    .build();

            subscription = subscriptionRepository.save(subscription);
            log.info("구독작성완료: code={}, orderId={}", subscriptionCode, orderId);

            // === 7. 주문명생성注文名生成 ===
            String orderName = generateOrderName(durationDays);

            return CreateSubscriptionPaymentResponse.builder()
                    .success(true)
                    .subscriptionCode(subscriptionCode)
                    .orderId(orderId)
                    .amount(totalPrice)
                    .orderName(orderName)
                    .expiresAt(expiresAt)
                    .message("결제준비가완료됬어요")
                    .build();

        } catch (Exception e) {
            log.error("구독결제작성에러: ", e);
            return CreateSubscriptionPaymentResponse.builder()
                    .success(false)
                    .message("결제작성에실패했어요: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Toss결제확인決済確認、Subscription유효화有効化
     */
    @Transactional
    public TossConfirmResponse confirmSubscriptionPayment(TossConfirmRequest request) {
        log.info("구독Toss결제확인개시: orderId={}", request.getOrderId());

        try {
            // === 1. Subscription취득取得 ===
            Subscription subscription = subscriptionRepository.findByOrderId(request.getOrderId())
                    .orElseThrow(() -> new RuntimeException("구독정보가안보여요"));

            // === 2. 금액검증金額検証 ===
            if (!subscription.getTotalPrice().equals(request.getAmount())) {
                return TossConfirmResponse.builder()
                        .success(false)
                        .message("결제금액이불일치")
                        .build();
            }

            // === 3. Toss결제확인決済確認 ===
            TossPaymentService.TossConfirmResult result = tossPaymentService.confirmPayment(
                    request.getPaymentKey(),
                    request.getOrderId(),
                    request.getAmount()
            );

            if (!result.isSuccess()) {
                subscription.setPaymentStatus(PaymentStatus.FAILED);
                subscriptionRepository.save(subscription);

                return TossConfirmResponse.builder()
                        .success(false)
                        .message(result.getErrorMessage())
                        .build();
            }

            // === 4. Subscription유효화有効化 ===
            LocalDateTime now = LocalDateTime.now();
            SubscriptionPrice priceInfo = subscriptionPriceRepository.findBySubscriptionType(subscription.getType())
                    .orElseThrow(() -> new RuntimeException("플랜정보가안보여요"));

            subscription.setPaymentStatus(PaymentStatus.COMPLETED);
            subscription.setTossPaymentKey(request.getPaymentKey());
            subscription.setPaymentMethod(result.getMethod());
            subscription.setPaidAt(now);
            subscription.setActive(1);  // 유효화有効化！
            subscription.setStartAt(now);
            subscription.setEndAt(now.plusDays(priceInfo.getDurationDays()));

            subscriptionRepository.save(subscription);

            log.info("구독결제완료: code={}, endAt={}", subscription.getCode(), subscription.getEndAt());

            return TossConfirmResponse.builder()
                    .success(true)
                    .status("COMPLETED")
                    .totalPrice(subscription.getTotalPrice())
                    .message("구독이유효화되었어요！")
                    .build();

        } catch (Exception e) {
            log.error("구독Toss결제확인에러: ", e);
            return TossConfirmResponse.builder()
                    .success(false)
                    .message("결제확인중에에러가발생했어요")
                    .build();
        }
    }

    // ============================================
    // 헬프 메소드 ヘルパーメソッド
    // ============================================

    private String generateSubscriptionOrderId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "SUB-" + timestamp + "-" + random;
    }

    private String generateSubscriptionCode() {
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "SUB-" + random;
    }

    private String generateOrderName(int durationDays) {
        return switch (durationDays) {
            case 7 -> "StudyCafe 7일간정액플랜";
            case 30 -> "StudyCafe 30일간정액플랜";
            case 365 -> "StudyCafe 연간정액플랜";
            default -> "StudyCafe " + durationDays + "일간정액플랜";
        };
    }

    private int getDiscountAmount(String couponCode) {
        // TODO: DB의discount테이블에서 취득
        return switch (couponCode) {
            case "DISCOUNT10" -> 500;
            case "WELCOME2024" -> 1000;
            case "VIP20" -> 2000;
            default -> 0;
        };
    }
}
