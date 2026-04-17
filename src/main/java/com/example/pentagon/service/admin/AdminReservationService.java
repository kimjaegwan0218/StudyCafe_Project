package com.example.pentagon.service.admin;

import com.example.pentagon.domain.enums.ReservationStatus;
import com.example.pentagon.dto.reservation.ReservationBoardItemDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AdminReservationService {

    List<ReservationBoardItemDTO> getDayReservations(LocalDate date, List<ReservationStatus> statuses);

}
