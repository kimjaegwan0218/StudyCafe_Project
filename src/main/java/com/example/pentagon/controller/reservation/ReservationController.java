package com.example.pentagon.controller.reservation;

import com.example.pentagon.dto.reservation.*;
import com.example.pentagon.service.ReservationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // ✅ 플랜 조회: 화면 Step1에서 1시간/2시간 플랜 버튼 구성에 사용
    @GetMapping("/time-prices")
    public List<TimePriceDTO> timePrices() {
        return reservationService.getTimePrices();
    }

    // ✅ 좌석 가용성 조회: 특정 날짜+시간+이용시간에 비어있는 좌석 계산
    // - Controller에서는 date/time을 파싱해서 LocalDateTime으로 만든다.
    // - Service는 정책 검증 + 점유 좌석 계산을 담당
    @GetMapping("/seats/availability")
    public SeatAvailabilityResponse seatAvailability(@RequestParam String date,
                                                     @RequestParam String time,
                                                     @RequestParam int hours)
    {
        LocalDateTime startAt = LocalDateTime.of(LocalDate.parse(date), LocalTime.parse(time));
        SeatAvailabilityResponse seatAvailability = reservationService.getSeatAvailability(startAt, hours);
        return seatAvailability;
    }

    // ✅ 예약 생성(홀드+PENDING 결제 생성)
    // - @AuthenticationPrincipal로 로그인한 userId를 서버에서 신뢰
    // - RequestBody(JSON)를 DTO(ReservationCreateRequest)로 자동 매핑
    @PostMapping("/reservations")
    public ReservationCreateResponse createReservation(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestBody ReservationCreateRequest request
    ) {
        ReservationCreateResponse reservationCreateResponse = reservationService.createReservation(userId, request);
        return reservationCreateResponse;
    }

    // ✅ 결제 결과 저장(상태 반영)
    @PostMapping("/reservations/{id}/payment")
    public PaymentSaveResponse savePaymentResult(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable("id") Long reservationId,
            @RequestBody PaymentSaveRequest request
    ) {
        return reservationService.savePaymentResult(userId, reservationId, request);
    }

    // ✅ 마이페이지 예약 목록
    @GetMapping("/reservations/me")
    public List<MyReservationItemDTO> myReservations(
            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        return reservationService.myReservations(userId);
    }

    // ✅ 개발용 결제 확정
    @PostMapping("/reservations/{id}/pay-success")
    public ReservationPaidResponse paySuccess(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable("id") Long reservationId
    ) {
        return reservationService.markPaid(userId, reservationId);
    }
}

