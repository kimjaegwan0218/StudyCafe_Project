package com.example.pentagon.repository;

import com.example.pentagon.domain.reservation.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findAllByActiveTrue();

    List<Seat> findByActiveTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.seatId = :seatId and s.active = true")
    Optional<Seat> findActiveByIdForUpdate(@Param("seatId") Long seatId);
}