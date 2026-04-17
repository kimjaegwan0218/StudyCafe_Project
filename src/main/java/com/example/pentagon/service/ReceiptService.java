package com.example.pentagon.service;

import com.example.pentagon.domain.reservation.Payment;
import com.example.pentagon.domain.reservation.Receipt;
import com.example.pentagon.domain.reservation.Reservation;
import com.example.pentagon.domain.reservation.Seat;
import com.example.pentagon.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * ReceiptService
 *
 * 전제:
 * - Receipt(receipt_id PK) -> Payment 1:1
 * - Payment는 discount 연관관계 없음 (discountId만 있음, 현재 프로젝트에서는 더미)
 * - 따라서 영수증 할인금액은 0으로 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptService {

    private final ReceiptRepository receiptRepository;

    /**
     * 영수증 PDF 생성 (단순 구현)
     *
     * ⚠️ 현재 프로젝트에서 PDF 라이브러리를 실제로 쓰고 있다면,
     * 아래 "텍스트 바이트" 부분을 기존 PDF 생성 코드로 바꾸고,
     * 핵심 값(할인 0 처리)만 유지하면 됨.
     */
    @Transactional(readOnly = true)
    public byte[] generateReceiptPdf(String receiptId) {

        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "영수증을 찾을 수 없습니다."));

        Payment payment = receipt.getPayment();
        if (payment == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "영수증에 결제가 연결되어 있지 않습니다.");
        }

        Reservation reservation = payment.getReservation();
        if (reservation == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "결제에 예약이 연결되어 있지 않습니다.");
        }

        Seat seat = reservation.getSeat();

        // ✅ discounts 더미: 연관관계 없음 -> 할인금액은 0 고정
        int discountAmount = 0;

        // 보기용 값들
        String seatLabel = (seat != null && seat.getType() != null)
                ? seat.getType().name()
                : "SEAT";

        Long seatId = (seat != null) ? seat.getSeatId() : null;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // ------------------------------------------------------------
        // ⚠️ 여기: 너희 기존 PDF 생성 코드가 있으면 그걸로 교체
        // 지금은 "컴파일/동작 확인용"으로 텍스트를 PDF 대신 내려주는 형태(임시)
        // ------------------------------------------------------------
        String body = ""
                + "=== StudyCafe Receipt ===\n"
                + "Receipt ID: " + receipt.getReceiptId() + "\n"
                + "Payment ID: " + payment.getPaymentId() + "\n"
                + "Reservation ID: " + reservation.getReservationId() + "\n"
                + "User: USER#" + reservation.getUserId() + "\n"
                + "Seat: " + seatLabel + (seatId != null ? (" (#" + seatId + ")") : "") + "\n"
                + "Start: " + (reservation.getStartAt() != null ? reservation.getStartAt().format(dtf) : "-") + "\n"
                + "Hours: " + reservation.getDurationHours() + "\n"
                + "Base: " + nvl(payment.getBasePrice()) + "\n"
                + "Surcharge: " + nvl(payment.getSurchargePrice()) + "\n"
                + "Discount: " + discountAmount + "\n"
                + "Total: " + nvl(payment.getTotalPrice()) + "\n"
                + "Status: " + (payment.getStatus() != null ? payment.getStatus().name() : "-") + "\n"
                + "PaidAt: " + (payment.getPaidAt() != null ? payment.getPaidAt().format(dtf) : "-") + "\n"
                + "IssuedAt: " + (receipt.getIssuedAt() != null ? receipt.getIssuedAt().format(dtf) : "-") + "\n";

        // 임시로 text bytes 반환 (기존에 PDF 생성 코드가 있다면 그걸로 대체)
        return body.getBytes(StandardCharsets.UTF_8);
    }

    private int nvl(Integer v) {
        return v == null ? 0 : v;
    }
}
