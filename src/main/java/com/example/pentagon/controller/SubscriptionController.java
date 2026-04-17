package com.example.pentagon.controller;

import com.example.pentagon.config.TossPaymentConfig;
import com.example.pentagon.domain.enums.PaymentStatus;
import com.example.pentagon.domain.reservation.Subscription;
import com.example.pentagon.domain.reservation.SubscriptionPrice;
import com.example.pentagon.dto.subscription.CreateSubscriptionPaymentRequest;
import com.example.pentagon.dto.subscription.CreateSubscriptionPaymentResponse;
import com.example.pentagon.dto.reservation.TossConfirmRequest;
import com.example.pentagon.dto.reservation.TossConfirmResponse;
import com.example.pentagon.dto.subscription.SubscriptionPaySuccessResponse;
import com.example.pentagon.dto.subscription.SubscriptionStatusResponse;
import com.example.pentagon.repository.SubscriptionPriceRepository;
import com.example.pentagon.repository.SubscriptionRepository;
import com.example.pentagon.service.SubscriptionPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 구독컨트롤러サブスクリプションコントローラ
 *
 * 배치장소配置場所: controller/SubscriptionController.java
 *
 * 구독관련API,페이지루팅サブスクリプション関連のAPI・ページルーティング
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionPaymentService subscriptionPaymentService;
    private final SubscriptionPriceRepository subscriptionPriceRepository;
    private final TossPaymentConfig tossConfig;
    private final SubscriptionRepository subscriptionRepository;
    // ============================================
    // 페이지표시ページ表示（Thymeleaf용用）
    // ============================================

    /**
     * 구독결제페이지를표시サブスク決済ページを表示
     *
     * URL: /subscription/payment?orderId=xxx&amount=xxx&orderName=xxx
     */
    @GetMapping("/subscription/payment")
    public String subscriptionPaymentPage(
            @RequestParam String subscriptionCode,
            @RequestParam String orderId,
            @RequestParam Integer amount,
            @RequestParam String orderName,
            Model model) {

        log.info("구독결제페이지표시: orderId={}", orderId);

        model.addAttribute("clientKey", tossConfig.getClientKey());
        model.addAttribute("subscriptionCode", subscriptionCode);
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);
        model.addAttribute("orderName", orderName);

        return "subscription/subscription-payment";  // templates/payment/subscription-payment.html
    }

    /**
     * 구독결제성공페이지サブスク決済成功ページ
     *
     * Toss결제성공후에 리다이렉트 되는 페이지決済完了後にリダイレクトされるページ
     */
    @GetMapping("/api/subscription-payments/toss/success")
    public String subscriptionPaymentSuccess(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Integer amount,
            Model model) {

        log.info("구독Toss결제성공콜백: orderId={}", orderId);

        // 결제확인처리決済確認処理
        TossConfirmRequest request = new TossConfirmRequest(paymentKey, orderId, amount);
        TossConfirmResponse result = subscriptionPaymentService.confirmSubscriptionPayment(request);

        model.addAttribute("result", result);

        if (result.isSuccess()) {
            return "subscription/subscription-success";  // templates/payment/subscription-success.html
        } else {
            model.addAttribute("errorMessage", result.getMessage());
            return "payment/fail";
        }
    }

    /**
     * 구독결제 실페 페이지サブスク決済失敗ページ
     */
    @GetMapping("/api/subscription-payments/toss/fail")
    public String subscriptionPaymentFail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String orderId,
            Model model) {

        log.warn("구독Toss결제실패: orderId={}, code={}, message={}", orderId, code, message);

        model.addAttribute("errorCode", code);
        model.addAttribute("errorMessage", message);

        return "payment/fail";  // templates/payment/fail.html
    }

    // ============================================
    // REST API
    // ============================================

    /**
     * 요금플랜 리스트 취득料金プラン一覧取得
     *
     * GET /api/subscription-plans
     */
    @GetMapping("/api/subscription-plans")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getSubscriptionPlans() {
        log.info("요금플랜리스트취득API");

        List<SubscriptionPrice> prices = subscriptionPriceRepository.findByActiveOrderByDurationDaysAsc(1);

        List<Map<String, Object>> result = prices.stream()
                .map(p -> Map.<String, Object>of(
                        "subscriptionType", p.getSubscriptionType(),
                        "subType", p.getDurationDays(),  // フロント互換用
                        "price", p.getPrice(),
                        "durationDays", p.getDurationDays()
                ))
                .toList();

        return ResponseEntity.ok(result);
    }

    /**
     * 구독결제를 작성サブスク決済を作成
     *
     * POST /api/subscription-payments
     */
    @PostMapping("/api/subscription-payments")
    @ResponseBody
    public ResponseEntity<CreateSubscriptionPaymentResponse> createSubscriptionPayment(
            @RequestBody CreateSubscriptionPaymentRequest request) {

        log.info("구독결제작성API: userId={}, type={}", request.getUserId(), request.getSubscriptionType());

        CreateSubscriptionPaymentResponse response = subscriptionPaymentService.createSubscriptionPayment(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Toss결제확인決済確認
     *
     * POST /api/subscription-payments/toss/confirm
     */
    @PostMapping("/api/subscription-payments/toss/confirm")
    @ResponseBody
    public ResponseEntity<TossConfirmResponse> confirmSubscriptionPayment(
            @RequestBody TossConfirmRequest request) {

        log.info("구독Toss결제확인API: orderId={}", request.getOrderId());

        TossConfirmResponse response = subscriptionPaymentService.confirmSubscriptionPayment(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping({"/api/subscriptions/me", "/api/subscription/me"})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> me(
            Authentication authentication,
            @AuthenticationPrincipal(expression = "user.id") Long userId
    ) {
        String principalType = null;
        if (authentication != null && authentication.getPrincipal() != null) {
            principalType = authentication.getPrincipal().getClass().getName();
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("userIdFromPrincipal", userId);
        out.put("principalType", principalType);

        // ✅ userId 자체가 안 나오면 여기서부터가 문제(=principal 표현식/인증객체 문제)
        if (userId == null) {
            out.put("active", false);
            out.put("reason", "NO_USER_ID_FROM_PRINCIPAL");
            return ResponseEntity.ok(out);
        }

        Optional<Subscription> opt = subscriptionRepository.findTopByUser_IdOrderByEndAtDesc(userId);

        if (opt.isEmpty()) {
            out.put("active", false);
            out.put("reason", "NO_SUBSCRIPTION_FOR_USER");
            return ResponseEntity.ok(out);
        }

        Subscription s = opt.get();
        LocalDateTime now = LocalDateTime.now();

        boolean dbActive = (s.getActive() != null && s.getActive() == 1);
        boolean paid = (s.getPaymentStatus() == PaymentStatus.COMPLETED);
        boolean validEndAt = (s.getEndAt() == null) || s.getEndAt().isAfter(now);

        // ✅ 여기서 active 판정(일단 정석)
        boolean active = dbActive && paid && validEndAt;

        out.put("active", active);

        // ---- 디버그용: 뭐가 false인지 바로 보이게 ----
        out.put("dbActive", s.getActive());
        out.put("paymentStatus", String.valueOf(s.getPaymentStatus()));
        out.put("startAt", s.getStartAt());
        out.put("endAt", s.getEndAt());
        out.put("serverNow", now);
        out.put("validEndAt", validEndAt);
        out.put("code", s.getCode());
        out.put("subscriptionType", s.getType());
        out.put("reason", "EVAL"); // 평가 결과

        return ResponseEntity.ok(out);
    }


    private SubscriptionStatusResponse toResp(Subscription s) {
        return SubscriptionStatusResponse.builder()
                .active(true)
                .code(s.getCode())
                .subscriptionType(s.getType())
                .startAt(s.getStartAt())
                .endAt(s.getEndAt())
                .build();
    }
}
