package com.example.pentagon.service;

import com.example.pentagon.domain.enums.PaymentStatus;
import com.example.pentagon.domain.reservation.Payment;
import com.example.pentagon.domain.reservation.Reservation;
import com.example.pentagon.domain.reservation.Seat;
import com.example.pentagon.domain.reservation.TimePrice;
import com.example.pentagon.dto.reservation.*;
import com.example.pentagon.repository.PaymentRepository;
import com.example.pentagon.repository.ReservationRepository;
import com.example.pentagon.repository.SeatRepository;
import com.example.pentagon.repository.TimePriceRepository;
import com.example.pentagon.repository.SubscriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class   ReservationService {

    // ✅ Service가 직접 DB를 만지는 게 아니라, Repository를 통해서만 접근한다.
    private final SeatRepository seatRepository;
    private final TimePriceRepository timePriceRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;

    // ✅ 정책: 예약 생성 후 결제까지 허용되는 시간(홀드 시간)
    // - 이 시간이 지나면 “점유”로 치지 않아서 좌석이 다시 풀린다.
    private static final Duration PAYMENT_HOLD = Duration.ofMinutes(5);

    public ReservationService(
            SeatRepository seatRepository,
            TimePriceRepository timePriceRepository,
            ReservationRepository reservationRepository,
            PaymentRepository paymentRepository,
            SubscriptionRepository subscriptionRepository
    ) {
        this.seatRepository = seatRepository;
        this.timePriceRepository = timePriceRepository;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    // -------------------------
    // 1) TimePrice: 이용 플랜(시간요금) 조회
    // -------------------------
    @Transactional(readOnly = true)
    public List<TimePriceDTO> getTimePrices() {

        // ✅ time_prices 테이블 전체 조회
        // ✅ durationHours 기준 정렬(프론트에서 버튼 순서가 자연스럽게 보이게)
        // ✅ 엔티티(TimePrice) → DTO(TimePriceDTO) 변환
        return timePriceRepository.findAll().stream()
                .sorted(Comparator.comparing(TimePrice::getDurationHours))
                .map(tp -> new TimePriceDTO(tp.getDurationHours(), tp.getPrice()))
                .toList();
    }

    // -------------------------
    // 2) Seat availability: 특정 시간대 좌석 점유(open/occupied) 계산
    // -------------------------
    @Transactional(readOnly = true)
    public SeatAvailabilityResponse getSeatAvailability(LocalDateTime startAt, int hours) {

        // ✅ 예약 정책 검증(시간 단위, 10분 단위, 과거 금지 등)
        validateTimePolicy(startAt, hours);

        // ✅ 새 예약의 끝시간을 계산 (endAt는 “겹침 판정”에서 중요)
        LocalDateTime endAt = startAt.plusHours(hours);

        // ✅ now는 “홀드 만료 여부 판단”에 필요
        LocalDateTime now = LocalDateTime.now();

        // ✅ 활성 좌석만 조회 (운영 중인 좌석)
        List<Seat> activeSeats = seatRepository.findAllByActiveTrue();

        // ✅ 이 시간대에 점유로 판정되는 seat_id 목록
        // - 결제완료(paid_at not null) OR 홀드중(payment_expires_at > now)
        List<Long> occupiedIds = reservationRepository.findOccupiedSeatIds(startAt, endAt, now);

        // ✅ contains 빠르게 하려고 Set으로 변환 (List.contains는 O(n))
        Set<Long> occupiedSet = new HashSet<>(occupiedIds);

        // ✅ 좌석별로 “open / occupied” 상태를 만들어 DTO로 내려줌
        List<SeatAvailabilityItemDTO> items = activeSeats.stream()
                .map(seat -> new SeatAvailabilityItemDTO(
                        seat.getSeatId(),
                        seat.getType(),
                        seat.getSurchargePrice(),
                        occupiedSet.contains(seat.getSeatId()) ? "occupied" : "open"
                ))
                .toList();

        return new SeatAvailabilityResponse(items);
    }

    // -------------------------
    // 3) 예약 생성 (PENDING 홀드) + 결제 레코드(PENDING) 생성
    // -------------------------
    @Transactional
    public ReservationCreateResponse createReservation(Long userId, ReservationCreateRequest req) {

        // ✅ date + time 문자열 → LocalDateTime으로 파싱
        // - 형식이 틀리면 400 BAD_REQUEST
        LocalDateTime startAt = parseStartAt(req.date(), req.time());

        // ✅ 이용 시간(시간 단위)
        int hours = req.durationHours();

        // ✅ 정책 검증
        validateTimePolicy(startAt, hours);

        // ✅ 동시성 방지(핵심)
        // - 같은 좌석에 동시에 예약이 들어오는 상황에서 중복 예약을 막기 위해
        // - 좌석 row를 FOR UPDATE로 잠근다.
        Seat seat = seatRepository.findActiveByIdForUpdate(req.seatId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용할 수 없는 좌석입니다."));

        LocalDateTime endAt = startAt.plusHours(hours);
        LocalDateTime now = LocalDateTime.now();

        // ✅ 겹침 검사
        // - 이 좌석에서 (결제완료 or 홀드중) 예약이 겹치면 예약 생성 금지
        boolean overlapping = reservationRepository.existsOverlapping(seat.getSeatId(), startAt, endAt, now) == 1;
        if (overlapping) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 예약된 시간대입니다.");
        }

        // ✅ 시간 요금(TimePrice) 조회
        // - durationHours를 PK로 쓰는 구조
        TimePrice tp = timePriceRepository.findById(hours)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 이용 시간입니다."));

        int originalBasePrice = tp.getPrice();       // 원래 기본요금(플랜 가격)
        int surcharge = seat.getSurchargePrice();    // 좌석 추가요금

        // ✅ 구독 정책(B 정책): 구독 있으면 기본요금은 무료(=0), 좌석추가요금만 결제
        boolean hasSubscription = subscriptionRepository.existsByUserIdAndActiveTrue(userId);
        int chargedBasePrice = hasSubscription ? 0 : originalBasePrice;

        // ✅ 최종 결제금액
        int total = chargedBasePrice + surcharge;

        // ✅ Reservation 생성
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setSeat(seat);
        reservation.setStartAt(startAt);
        reservation.setDurationHours(hours);

        // ✅ 결제 만료 시간(홀드)
        // - paid_at이 찍히기 전까지는 “홀드중” 상태로 간주
        reservation.setPaymentExpiresAt(now.plus(PAYMENT_HOLD));

        reservation = reservationRepository.save(reservation);

        // ✅ Payment 생성 (영수증/결제 상태 저장용)
        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setBasePrice(chargedBasePrice);  // 구독이면 0 저장
        payment.setSurchargePrice(surcharge);
        payment.setTotalPrice(total);
        payment.setDiscountId(null);
        payment.setStatus(PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);

        // ✅ 프론트로 “예약/결제 생성 결과” 내려줌
        return new ReservationCreateResponse(
                reservation.getReservationId(),
                seat.getSeatId(),
                seat.getType(),
                reservation.getStartAt(),
                reservation.getDurationHours(),
                chargedBasePrice,
                surcharge,
                total,
                reservation.getPaymentExpiresAt(),
                payment.getPaymentId()
        );
    }

    // -------------------------
    // 4) 결제 결과 저장(외부 결제 API 결과를 DB에 반영)
    // -------------------------
    @Transactional
    public PaymentSaveResponse savePaymentResult(Long userId, Long reservationId, PaymentSaveRequest req) {

        // ✅ 예약 row 잠금(결제 콜백이 중복으로 올 수 있어서)
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."));

        // ✅ 본인 예약만 처리 가능
        if (!Objects.equals(reservation.getUserId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 예약만 처리할 수 있습니다.");
        }

        LocalDateTime now = LocalDateTime.now();

        // ✅ 결제 만료 후에는 PAID 처리 금지
        // - (결제 성공 콜백이 늦게 와도) 만료된 홀드면 인정하지 않겠다는 정책
        if (reservation.getPaidAt() == null
                && reservation.getPaymentExpiresAt() != null
                && reservation.getPaymentExpiresAt().isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.GONE, "결제 기한이 만료되었습니다.");
        }

        // ✅ reservationId로 Payment 조회
        Payment payment = paymentRepository.findByReservation_ReservationId(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "결제 레코드가 없습니다."));

        // ✅ 멱등 처리: 이미 PAID면 같은 결과로 응답
        if (payment.getStatus() == PaymentStatus.PAID) {
            return new PaymentSaveResponse(reservationId, payment.getPaymentId(), payment.getStatus().name(), payment.getPaidAt());
        }

        // ✅ status 문자열 → enum 변환
        PaymentStatus status;
        try {
            status = PaymentStatus.valueOf(req.status());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 결제 status 입니다.");
        }

        payment.setStatus(status);

        // ✅ 결제 성공이면 paidAt 기록 + Reservation도 확정 처리
        if (status == PaymentStatus.PAID) {
            LocalDateTime paidAt = now;

            // paidAt을 외부에서 줄 수도 있으니 파싱 시도(실패하면 now 유지)
            if (req.paidAt() != null && !req.paidAt().isBlank()) {
                try { paidAt = LocalDateTime.parse(req.paidAt()); } catch (Exception ignore) {}
            }
            payment.setPaidAt(paidAt);
            reservation.setPaidAt(paidAt);
        }

        // ✅ 저장은 JPA dirty checking으로 될 수도 있지만,
        // PaymentRepository.save를 명시적으로 호출하는 편이 명확할 때도 많다(현재는 생략되어도 트랜잭션 내라 반영될 가능성 큼)

        return new PaymentSaveResponse(
                reservationId,
                payment.getPaymentId(),
                payment.getStatus().name(),
                payment.getPaidAt()
        );
    }

    // -------------------------
    // 5) 마이페이지 예약 목록
    // -------------------------
    @Transactional(readOnly = true)
    public List<MyReservationItemDTO> myReservations(Long userId) {

        LocalDateTime now = LocalDateTime.now();

        // ✅ seat fetch join으로 가져와서 seatType 같이 내려줌(N+1 방지)
        return reservationRepository.findAllMineWithSeat(userId).stream()
                .map(r -> {
                    LocalDateTime startAt = r.getStartAt();
                    Integer hours = r.getDurationHours();

                    // ✅ 종료시간 계산은 프론트가 아니라 서버가 해주는 편이 정책 일관성이 좋다
                    LocalDateTime endAt = (startAt != null && hours != null) ? startAt.plusHours(hours) : null;

                    LocalDateTime paidAt = r.getPaidAt();
                    LocalDateTime expiresAt = r.getPaymentExpiresAt();

                    // ✅ “표시용 한글 상태” 계산
                    String paymentStateKo = paymentStateKo(paidAt, expiresAt, now);
                    String useStateKo = useStateKo(paidAt, expiresAt, startAt, endAt, now);

                    return new MyReservationItemDTO(
                            r.getReservationId(),
                            r.getSeat().getSeatId(),
                            seatTypeKo(r.getSeat().getType()),
                            startAt,
                            endAt,
                            hours,
                            paymentStateKo,
                            useStateKo,
                            paidAt,
                            expiresAt
                    );
                })
                .toList();
    }

    // -------------------------
    // Helpers: 파싱/정책 검증
    // -------------------------
    private static LocalDateTime parseStartAt(String date, String time) {
        try {
            LocalDate d = LocalDate.parse(date);
            LocalTime t = LocalTime.parse(time);
            return LocalDateTime.of(d, t);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date/time 형식이 올바르지 않습니다.");
        }
    }

    private static void validateTimePolicy(LocalDateTime startAt, int hours) {
        // ✅ 이용 시간 범위 정책
        if (hours < 1 || hours > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "durationHours는 1~10만 가능합니다.");
        }

        // ✅ 10분 단위 시작 정책(00/10/20/30/40/50)
        if (startAt.getMinute() % 10 != 0 || startAt.getSecond() != 0 || startAt.getNano() != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "예약 시작 시간은 10분 단위여야 합니다.");
        }

        // ✅ 과거 예약 금지
        if (startAt.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "과거 시간은 예약할 수 없습니다.");
        }
    }

    // -------------------------
    // 개발용: 결제 확정(테스트용)
    // -------------------------
    @Transactional
    public ReservationPaidResponse markPaid(Long userId, Long reservationId) {

        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "예약이 없습니다."));

        if (!Objects.equals(r.getUserId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 예약만 처리할 수 있습니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (r.getPaymentExpiresAt() != null && r.getPaymentExpiresAt().isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "결제 기한이 만료되었습니다.");
        }

        if (r.getPaidAt() == null) {
            r.setPaidAt(now);
        }

        Payment p = paymentRepository.findByReservation_ReservationId(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "결제 레코드가 없습니다."));

        if (p.getStatus() != PaymentStatus.PAID) {
            p.setStatus(PaymentStatus.PAID);
        }
        p.setPaidAt(r.getPaidAt());

        paymentRepository.save(p);

        return new ReservationPaidResponse(r.getReservationId(), r.getPaidAt());
    }

    // -------------------------
    // 표시용 한글 변환/상태 계산
    // -------------------------
    private String seatTypeKo(Object seatType) {
        if (seatType == null) return "좌석";
        String key = seatType.toString();

        return switch (key) {
            case "SINGLE" -> "1인석";
            case "COUPLE" -> "커플석";
            case "MEETING" -> "회의실";
            default -> key;
        };
    }

    private String paymentStateKo(LocalDateTime paidAt, LocalDateTime paymentExpiresAt, LocalDateTime now) {
        if (paidAt != null) return "결제완료";
        if (paymentExpiresAt != null && paymentExpiresAt.isBefore(now)) return "결제만료";
        return "결제대기";
    }

    /**
     * 이용상태 정책:
     * - 미결제: 결제기한 지났으면 예약만료, 아니면 결제대기
     * - 결제완료: 시간 기준으로 이용예정/이용중/이용완료
     */
    private String useStateKo(LocalDateTime paidAt,
                              LocalDateTime paymentExpiresAt,
                              LocalDateTime startAt,
                              LocalDateTime endAt,
                              LocalDateTime now) {

        if (paidAt == null) {
            if (paymentExpiresAt != null && paymentExpiresAt.isBefore(now)) return "예약만료";
            return "결제대기";
        }

        if (startAt == null || endAt == null) return "이용예정";

        if (now.isBefore(startAt)) return "이용예정";
        if (!now.isAfter(endAt)) return "이용중"; // now <= endAt
        return "이용완료";
    }
}

