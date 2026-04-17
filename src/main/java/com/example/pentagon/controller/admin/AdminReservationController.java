package com.example.pentagon.controller.admin;


import com.example.pentagon.domain.enums.ReservationStatus;
import com.example.pentagon.dto.reservation.ReservationBoardItemDTO;
import com.example.pentagon.service.admin.AdminReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {

    private final AdminReservationService adminReservationService;

    /**
     * 예) GET /api/admin/reservations/day?date=2026-01-22
     * 예) GET /api/admin/reservations/day?date=2026-01-22&statuses=PENDING&statuses=RESERVED
     */
    @GetMapping("/day")
    public List<ReservationBoardItemDTO> getDayBoard(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "statuses", required = false) List<ReservationStatus> statuses
    ) {
        return adminReservationService.getDayReservations(date, statuses);
    }
}
