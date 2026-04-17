package com.example.pentagon.dto.reservation;


import com.example.pentagon.domain.enums.ReservationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ReservationBoardItemDTO {

    private Long reservationId;

    // 좌석 (너는 18개 고정이니까 번호만 내려줘도 충분)
    private Long seatId;        // 필요하면

    // 사용자
    private Long userId;
    private String userName;

    // 시간
    private LocalDateTime startAt;

    // ⚠️ 중요: 엔티티 필드는 durationHours지만, 실제 의미는 "분"이므로 DTO는 minutes로 통일
    private Integer durationHours;

    // 상태
    com.example.pentagon.domain.enums.ReservationStatus status;
}
