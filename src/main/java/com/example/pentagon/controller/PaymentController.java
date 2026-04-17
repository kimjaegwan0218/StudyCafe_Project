package com.example.pentagon.controller;

import com.example.pentagon.config.TossPaymentConfig;
import com.example.pentagon.dto.reservation.ApiResponse;
import com.example.pentagon.dto.reservation.CreateReservationRequest;
import com.example.pentagon.dto.reservation.CreateReservationResponse;
import com.example.pentagon.dto.reservation.RefundRequest;
import com.example.pentagon.dto.reservation.TossConfirmRequest;
import com.example.pentagon.dto.reservation.TossConfirmResponse;
import com.example.pentagon.service.PaymentService;
import com.example.pentagon.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 결제 컨트롤러 (통합본)
 *
 * - 외부 DTO(com.example.pentagon.dto.reservation.*)만 사용
 * - PaymentService는 컨트롤러가 쓰는 DTO를 그대로 리턴하도록 맞춘 상태를 전제
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final ReceiptService receiptService;
    private final TossPaymentConfig tossConfig;

    // ============================================================
    // 페이지 표시 (Thymeleaf)
    // ============================================================

    /**
     * 결제페이지 표시
     * URL: /booking/payment?reservationId=xxx&paymentId=yyy&orderId=...&amount=...&orderName=...
     */
    @GetMapping("/booking/payment")
    public String paymentPage(
            @RequestParam Long reservationId,
            @RequestParam Long paymentId,
            @RequestParam String orderId,
            @RequestParam Integer amount,
            @RequestParam String orderName,
            Model model
    ) {
        log.info("결제페이지 표시: reservationId={}, paymentId={}, orderId={}", reservationId, paymentId, orderId);

        model.addAttribute("clientKey", tossConfig.getClientKey());
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);
        model.addAttribute("orderName", orderName);
        model.addAttribute("successUrl", tossConfig.getSuccessUrl());
        model.addAttribute("failUrl", tossConfig.getFailUrl());
        model.addAttribute("reservationId", reservationId);
        model.addAttribute("paymentId", paymentId);

        return "payment/payment";
    }

    /**
     * Toss 결제 성공 리다이렉트 페이지
     * GET /api/payments/toss/success?paymentKey=...&orderId=...&amount=...
     */
    @GetMapping("/api/payments/toss/success")
    public String paymentSuccess(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Integer amount,
            Model model
    ) {
        log.info("Toss 결제 성공 콜백: orderId={}", orderId);

        // ✅ 외부 DTO로 생성 (팀원 컨트롤러 방식 유지)
        TossConfirmRequest req = new TossConfirmRequest();
        req.setPaymentKey(paymentKey);
        req.setOrderId(orderId);
        req.setAmount(amount);

        // ✅ 서비스도 외부 TossConfirmResponse를 리턴해야 타입 에러 없음
        TossConfirmResponse result = paymentService.confirmTossPayment(req);

        model.addAttribute("result", result);

        if (result != null && result.isSuccess()) {
            return "payment/success";
        } else {
            model.addAttribute("errorMessage", result != null ? result.getMessage() : "결제 확인 실패");
            return "payment/fail";
        }
    }

    /**
     * Toss 결제 실패 리다이렉트 페이지
     * GET /api/payments/toss/fail?code=...&message=...&orderId=...
     */
    @GetMapping("/api/payments/toss/fail")
    public String paymentFail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String orderId,
            Model model
    ) {
        log.warn("Toss 결제 실패: orderId={}, code={}, message={}", orderId, code, message);

        model.addAttribute("errorCode", code);
        model.addAttribute("errorMessage", message);

        return "payment/fail";
    }

    // ============================================================
    // REST API
    // ============================================================

    /**
     * 예약 + 결제 레코드 생성 (팀원 컨트롤러 호환)
     * POST /api/reservations
     */
    @PostMapping("/api/reservations/payment")
    @ResponseBody
    public ResponseEntity<CreateReservationResponse> createReservation(
            @RequestBody CreateReservationRequest request
    ) {
        log.info("예약 생성 API: userId={}", request != null ? request.getUserId() : null);

        CreateReservationResponse response = paymentService.createReservationAndPayment(request);

        if (response != null && response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Toss 결제 확인(승인)
     * POST /api/payments/toss/confirm
     */
    @PostMapping("/api/payments/toss/confirm")
    @ResponseBody
    public ResponseEntity<TossConfirmResponse> confirmPayment(
            @RequestBody TossConfirmRequest request
    ) {
        log.info("Toss 결제 확인 API: orderId={}", request != null ? request.getOrderId() : null);

        TossConfirmResponse response = paymentService.confirmTossPayment(request);

        if (response != null && response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 환불
     * POST /api/payments/refund
     */
    @PostMapping("/api/payments/refund")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> refundPayment(
            @RequestBody RefundRequest request
    ) {
        log.info("환불 API: paymentId={}", request != null ? request.getPaymentId() : null);

        ApiResponse<Void> response = paymentService.processRefund(request);

        if (response != null && response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 영수증 PDF 다운로드
     * GET /api/receipts/{receiptId}
     */
    @GetMapping("/api/receipts/{receiptId}")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable String receiptId) {
        log.info("영수증 다운로드: receiptId={}", receiptId);

        try {
            byte[] pdfBytes = receiptService.generateReceiptPdf(receiptId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "receipt_" + receiptId + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("영수증 생성 오류: ", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 쿠폰 검증 (임시)
     * GET /api/coupons/validate?code=XXX
     */
    @GetMapping("/api/coupons/validate")
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> validateCoupon(@RequestParam String code) {

        log.info("쿠폰 검증: code={}", code);

        java.util.Map<String, Integer> validCoupons = java.util.Map.of(
                "DISCOUNT10", 500,
                "WELCOME2024", 1000,
                "VIP20", 2000
        );

        if (validCoupons.containsKey(code)) {
            return ResponseEntity.ok(ApiResponse.ok("유효한 쿠폰입니다", java.util.Map.of(
                    "code", code,
                    "discount", validCoupons.get(code)
            )));
        }
        return ResponseEntity.ok(ApiResponse.error("무효한 쿠폰입니다"));
    }
}
