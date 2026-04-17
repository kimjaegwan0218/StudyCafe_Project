package com.example.pentagon.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeatSurchargeResponse {
    private String type;           // COUPLE / MEETING
    private Integer surchargePrice;
}
