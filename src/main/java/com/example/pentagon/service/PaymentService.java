package com.example.pentagon.service;

import com.example.pentagon.domain.enums.PaymentStatus;
import com.example.pentagon.domain.reservation.Payment;
import com.example.pentagon.domain.reservation.Receipt;
import com.example.pentagon.domain.reservation.Reservation;
import com.example.pentagon.repository.PaymentRepository;
import com.example.pentagon.repository.ReceiptRepository;
import com.example.pentagon.repository.ReservationRepository;
import com.example.pentagon.dto.reservation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final ReceiptRepository receiptRepository;
    private final TossPaymentService tossPaymentService;

    // ============================================================
    // 0) 팀원 컨트롤러 호환: /api/reservations
    //    CreateReservationResponse createReservationAndPayment(CreateReservationRequest)
    // ============================================================
    @Transactional
    public CreateReservationResponse createReservationAndPayment(CreateReservationRequest request) {
        try {
            if (request == null) {
                return CreateReservationResponse.builder()
                        .success(false)
                        .message("요청이 비었습니다.")
                        .build();
            }

            Long userId = request.getUserId();
            Long seatId = request.getSeatId();
            Integer durationHours = request.getDurationHours();

            if (userId == null) {
                return CreateReservationResponse.builder().success(false).message("userId가 없습니다.").build();
            }
            if (seatId == null) {
                return CreateReservationResponse.builder().success(false).message("seatId가 없습니다.").build();
            }
            if (durationHours == null || durationHours < 1) {
                return CreateReservationResponse.builder().success(false).message("durationHours가 올바르지 않습니다.").build();
            }

            // startAt이 있으면 date/time로 변환해서 네 ReservationService에 맞춤
            LocalDateTime startAt = request.getStartAt();
            if (startAt == null) {
                return CreateReservationResponse.builder().success(false).message("startAt이 없습니다.").build();
            }

            String date = startAt.toLocalDate().toString();
            String time = startAt.toLocalTime().withNano(0).toString();
            // HH:mm 형태가 필요하면 아래 주석 해제
            // time = startAt.toLocalTime().withSecond(0).withNano(0).format(DateTimeFormatter.ofPattern("HH:mm"));

            ReservationCreateRequest innerReq =
                    new ReservationCreateRequest(date, time, durationHours, seatId);

            ReservationCreateResponse created = reservationService.createReservation(userId, innerReq);

            Long reservationId = created.reservationId();

            // 방금 생성된 Payment 찾고 READY 전환 + orderId 발급(0원이면 즉시 완료)
            Payment payment = paymentRepository.findByReservation_ReservationId(reservationId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "결제 레코드가 없습니다."));

            Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."));

            // 0원이면 즉시 완료
            if (payment.getTotalPrice() != null && payment.getTotalPrice() == 0) {
                LocalDateTime now = LocalDateTime.now();

                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(now);
                payment.setPaymentMethod("FREE");
                if (isBlank(payment.getOrderId())) payment.setOrderId(generateOrderId());

                reservation.setPaidAt(now);

                paymentRepository.save(payment);
                reservationRepository.save(reservation);

                String receiptId = ensureReceipt(payment);

                return CreateReservationResponse.builder()
                        .success(true)
                        .reservationId(reservationId)
                        .paymentId(payment.getPaymentId())
                        .orderId(payment.getOrderId())
                        .amount(0)
                        .orderName(buildOrderName(durationHours))
                        .expiresAt(null)
                        .message("0원 결제라 즉시 완료 처리되었습니다.")
                        .build();
            }

            // 일반 결제: orderId 발급 + READY
            if (isBlank(payment.getOrderId())) payment.setOrderId(generateOrderId());
            payment.setStatus(PaymentStatus.READY);
            paymentRepository.save(payment);

            return CreateReservationResponse.builder()
                    .success(true)
                    .reservationId(reservationId)
                    .paymentId(payment.getPaymentId())
                    .orderId(payment.getOrderId())
                    .amount(payment.getTotalPrice())
                    .orderName(buildOrderName(durationHours))
                    .expiresAt(reservation.getPaymentExpiresAt())
                    .message("결제 준비가 완료되었습니다.")
                    .build();

        } catch (Exception e) {
            log.error("createReservationAndPayment 오류", e);
            return CreateReservationResponse.builder()
                    .success(false)
                    .message("예약/결제 생성 실패: " + e.getMessage())
                    .build();
        }
    }

    // ============================================================
    // 1) 팀원 컨트롤러 호환: Toss Confirm
    //    TossConfirmResponse confirmTossPayment(TossConfirmRequest)
    // ============================================================
    @Transactional
    public TossConfirmResponse confirmTossPayment(TossConfirmRequest request) {
        try {
            if (request == null) {
                return TossConfirmResponse.builder().success(false).message("요청이 비었습니다.").build();
            }

            String orderId = request.getOrderId();
            String paymentKey = request.getPaymentKey();
            Integer amount = request.getAmount();

            if (isBlank(orderId)) return TossConfirmResponse.builder().success(false).message("orderId가 없습니다.").build();
            if (isBlank(paymentKey)) return TossConfirmResponse.builder().success(false).message("paymentKey가 없습니다.").build();
            if (amount == null) return TossConfirmResponse.builder().success(false).message("amount가 없습니다.").build();

            Payment payment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "orderId에 해당하는 결제가 없습니다."));

            Reservation reservation = payment.getReservation();
            if (reservation == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "결제에 연결된 예약이 없습니다.");
            }

            // 멱등
            if (isPaid(payment.getStatus())) {
                String receiptId = ensureReceipt(payment);
                return TossConfirmResponse.builder()
                        .success(true)
                        .paymentId(payment.getPaymentId())
                        .reservationId(reservation.getReservationId())
                        .status(payment.getStatus().name())
                        .totalPrice(payment.getTotalPrice())
                        .receiptId(receiptId)
                        .receiptUrl("/api/receipts/" + receiptId)
                        .message("이미 결제완료 상태입니다.")
                        .build();
            }

            // 금액 검증
            if (payment.getTotalPrice() == null || !payment.getTotalPrice().equals(amount)) {
                return TossConfirmResponse.builder()
                        .success(false)
                        .message("결제 금액이 일치하지 않습니다.")
                        .build();
            }

            // 만료 체크
            LocalDateTime now = LocalDateTime.now();
            if (reservation.getPaidAt() == null
                    && reservation.getPaymentExpiresAt() != null
                    && reservation.getPaymentExpiresAt().isBefore(now)) {

                payment.setStatus(PaymentStatus.EXPIRED);
                paymentRepository.save(payment);

                return TossConfirmResponse.builder()
                        .success(false)
                        .message("결제 기한이 만료되었습니다.")
                        .build();
            }

            // Toss 승인
            TossPaymentService.TossConfirmResult result =
                    tossPaymentService.confirmPayment(paymentKey, orderId, amount);

            if (!result.isSuccess()) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);

                return TossConfirmResponse.builder()
                        .success(false)
                        .message(result.getErrorMessage())
                        .build();
            }

            // 성공 처리
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTossPaymentKey(paymentKey);
            payment.setPaymentMethod(result.getMethod());
            payment.setPaidAt(now);

            reservation.setPaidAt(now);

            paymentRepository.save(payment);
            reservationRepository.save(reservation);

            String receiptId = ensureReceipt(payment);

            return TossConfirmResponse.builder()
                    .success(true)
                    .paymentId(payment.getPaymentId())
                    .reservationId(reservation.getReservationId())
                    .status(payment.getStatus().name())
                    .totalPrice(payment.getTotalPrice())
                    .receiptId(receiptId)
                    .receiptUrl("/api/receipts/" + receiptId)
                    .message("결제가 완료되었습니다.")
                    .build();

        } catch (Exception e) {
            log.error("confirmTossPayment 오류", e);
            return TossConfirmResponse.builder()
                    .success(false)
                    .message("결제 확인 중 오류: " + e.getMessage())
                    .build();
        }
    }

    // ============================================================
    // 2) 팀원 컨트롤러 호환: Refund
    //    ApiResponse<Void> processRefund(RefundRequest)
    // ============================================================
    @Transactional
    public ApiResponse<Void> processRefund(RefundRequest request) {
        Long paymentId = (request != null) ? request.getPaymentId() : null;
        if (paymentId == null) return ApiResponse.error("paymentId가 없습니다.");

        String cancelReason = null;
        try {
            // dto가 getCancelReason()을 가지고 있으면 사용
            cancelReason = request.getCancelReason();
        } catch (Exception ignore) {
            // 없으면 null
        }

        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (payment == null) return ApiResponse.error("결제 정보를 찾을 수 없습니다.");

        Reservation reservation = payment.getReservation();
        if (reservation == null) return ApiResponse.error("결제에 연결된 예약이 없습니다.");

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            return ApiResponse.ok("이미 환불된 결제입니다.", null);
        }

        // 0원은 toss 취소 불필요
        if (payment.getTotalPrice() != null && payment.getTotalPrice() == 0) {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            reservation.setPaidAt(null);
            reservation.setPaymentExpiresAt(LocalDateTime.now().minusSeconds(1));
            reservationRepository.save(reservation);

            return ApiResponse.ok("0원 결제라 환불 처리만 완료했습니다.", null);
        }

        if (!isBlank(payment.getTossPaymentKey())) {
            TossPaymentService.TossCancelResult cancel =
                    tossPaymentService.cancelPayment(payment.getTossPaymentKey(), cancelReason);

            if (!cancel.isSuccess()) {
                return ApiResponse.error("환불 실패: " + cancel.getErrorMessage());
            }
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        reservation.setPaidAt(null);
        reservation.setPaymentExpiresAt(LocalDateTime.now().minusSeconds(1));
        reservationRepository.save(reservation);

        return ApiResponse.ok("환불이 완료되었습니다.", null);
    }

    // ============================================================
    // Helpers
    // ============================================================

    private boolean isPaid(PaymentStatus status) {
        if (status == null) return false;
        return "COMPLETED".equals(status.name()) || "PAID".equals(status.name());
    }

    private String buildOrderName(int hours) {
        return "StudyCafe " + hours + "시간팩";
    }

    private String ensureReceipt(Payment payment) {
        var existing = receiptRepository.findByPayment_PaymentId(payment.getPaymentId());
        if (existing.isPresent()) return existing.get().getReceiptId();

        String receiptId = generateReceiptId();
        Receipt receipt = Receipt.builder()
                .receiptId(receiptId)
                .payment(payment)
                .build();
        receiptRepository.save(receipt);
        return receiptId;
    }

    private String generateOrderId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "ORD-" + timestamp + "-" + random;
    }

    private String generateReceiptId() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "RC-" + date + "-" + random;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
