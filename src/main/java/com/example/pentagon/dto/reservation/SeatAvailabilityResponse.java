package com.example.pentagon.dto.reservation;

import java.util.List;

public record SeatAvailabilityResponse(List<SeatAvailabilityItemDTO> seats) {}