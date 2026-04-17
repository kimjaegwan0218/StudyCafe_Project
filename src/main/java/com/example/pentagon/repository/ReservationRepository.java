package com.example.pentagon.repository;

import com.example.pentagon.domain.enums.ReservationStatus;
import com.example.pentagon.domain.reservation.Reservation;
import com.example.pentagon.dto.reservation.ReservationBoardItemDTO;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * 예약 단건 조회 + Seat fetch + PESSIMISTIC_WRITE 락
     * - 네 Reservation PK 필드: reservationId
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r join fetch r.seat where r.reservationId = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") Long id);

    /**
     * 겹침 체크 (MariaDB native)
     * 점유 조건:
     * - paid_at is not null (결제확정)
     * - 또는 paid_at is null AND payment_expires_at > now (홀드 유지중)
     */
    @Query(value = """
        select exists(
          select 1
          from reservations r
          where r.seat_id = :seatId
            and r.start_at < :endAt
            and date_add(r.start_at, interval r.duration_hours hour) > :startAt
            and (
              r.paid_at is not null
              or (r.paid_at is null and r.payment_expires_at is not null and r.payment_expires_at > :now)
            )
        )
        """, nativeQuery = true)
    int existsOverlapping(
            @Param("seatId") Long seatId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("now") LocalDateTime now
    );

    /**
     * 특정 시간대에 점유된 seat_id 목록
     */
    @Query(value = """
        select distinct r.seat_id
        from reservations r
        where r.start_at < :endAt
          and date_add(r.start_at, interval r.duration_hours hour) > :startAt
          and (
            r.paid_at is not null
            or (r.paid_at is null and r.payment_expires_at is not null and r.payment_expires_at > :now)
          )
        """, nativeQuery = true)
    List<Long> findOccupiedSeatIds(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("now") LocalDateTime now
    );

    /**
     * 내 예약 목록(Seat fetch join)
     * - 네 Reservation은 User 엔티티가 없고 userId(Long)만 저장
     */
    @Query("select r from Reservation r join fetch r.seat where r.userId = :userId order by r.startAt desc")
    List<Reservation> findAllMineWithSeat(@Param("userId") Long userId);

    @Query("""
select new com.example.pentagon.dto.reservation.ReservationBoardItemDTO(
    r.reservationId,
    s.seatId,
    r.userId,
    u.name,
    r.startAt,
    r.durationHours,
    case
        when r.paidAt is null
             and r.paymentExpiresAt is not null
             and r.paymentExpiresAt > current_timestamp
            then com.example.pentagon.domain.enums.ReservationStatus.PENDING

        when r.paidAt is null
            then com.example.pentagon.domain.enums.ReservationStatus.CANCELLED

        when r.startAt > current_timestamp
            then com.example.pentagon.domain.enums.ReservationStatus.RESERVED

        when timestampadd(hour, r.durationHours, r.startAt) <= current_timestamp
            then com.example.pentagon.domain.enums.ReservationStatus.COMPLETED

        else com.example.pentagon.domain.enums.ReservationStatus.USING
    end
)
from Reservation r
join r.seat s
left join com.example.pentagon.domain.User u on u.id = r.userId
where r.startAt >= :start
  and r.startAt < :end
  and (
    case
        when r.paidAt is null
             and r.paymentExpiresAt is not null
             and r.paymentExpiresAt > current_timestamp
            then com.example.pentagon.domain.enums.ReservationStatus.PENDING

        when r.paidAt is null
            then com.example.pentagon.domain.enums.ReservationStatus.CANCELLED

        when r.startAt > current_timestamp
            then com.example.pentagon.domain.enums.ReservationStatus.RESERVED

        when timestampadd(hour, r.durationHours, r.startAt) <= current_timestamp
            then com.example.pentagon.domain.enums.ReservationStatus.COMPLETED

        else com.example.pentagon.domain.enums.ReservationStatus.USING
    end
  ) in :statuses
order by s.seatId asc, r.startAt asc
""")
    List<ReservationBoardItemDTO> findBoardItemsByDay(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("statuses") List<com.example.pentagon.domain.enums.ReservationStatus> statuses
    );

    /**
     * ✅ statuses 필터 없는 버전 (statuses가 null/empty일 때 이걸 호출)
     */
    @Query("""
    select new com.example.pentagon.dto.reservation.ReservationBoardItemDTO(
        r.reservationId,
        s.seatId,
        r.userId,
        u.name,
        r.startAt,
        r.durationHours,
        case
            when r.paidAt is null
                 and r.paymentExpiresAt is not null
                 and r.paymentExpiresAt > current_timestamp
                then com.example.pentagon.domain.enums.ReservationStatus.PENDING

            when r.paidAt is null
                then com.example.pentagon.domain.enums.ReservationStatus.CANCELLED

            when r.startAt > current_timestamp
                then com.example.pentagon.domain.enums.ReservationStatus.RESERVED

            when timestampadd(hour, r.durationHours, r.startAt) <= current_timestamp
                then com.example.pentagon.domain.enums.ReservationStatus.COMPLETED

            else com.example.pentagon.domain.enums.ReservationStatus.USING
        end
    )
    from Reservation r
    join r.seat s
    left join com.example.pentagon.domain.User u on u.id = r.userId
    where r.startAt >= :start
      and r.startAt < :end
    order by s.seatId asc, r.startAt asc
    """)
    List<ReservationBoardItemDTO> findBoardItemsByDayAll(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

}
