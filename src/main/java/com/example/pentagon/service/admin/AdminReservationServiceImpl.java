package com.example.pentagon.service.admin;


import com.example.pentagon.domain.User;
import com.example.pentagon.domain.enums.ReservationStatus;
import com.example.pentagon.domain.reservation.Reservation;
import com.example.pentagon.domain.reservation.Seat;
import com.example.pentagon.dto.reservation.ReservationBoardItemDTO;
import com.example.pentagon.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReservationServiceImpl implements AdminReservationService {

    private final ReservationRepository reservationRepository;

    @Override
    public List<ReservationBoardItemDTO> getDayReservations(LocalDate date, List<ReservationStatus> statuses) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        // ✅ 중요: statuses가 null/empty면 "필터 없는 쿼리"로 호출 (500 방지)
        if (statuses == null || statuses.isEmpty()) {
            return reservationRepository.findBoardItemsByDayAll(start, end);
        }

        return reservationRepository.findBoardItemsByDay(start, end, statuses);
    }

}
