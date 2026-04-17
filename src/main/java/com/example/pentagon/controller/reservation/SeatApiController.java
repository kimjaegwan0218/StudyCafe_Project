package com.example.pentagon.controller.reservation;

import com.example.pentagon.dto.reservation.SeatSurchargeResponse;
import com.example.pentagon.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequiredArgsConstructor
public class SeatApiController {

    private final SeatRepository seatRepository;

    @GetMapping("/api/seats/surcharges")
    public List<SeatSurchargeResponse> surcharges() {

        Map<String, Integer> maxByType = new HashMap<>();

        seatRepository.findByActiveTrue().forEach(seat -> {
            String type = String.valueOf(seat.getType()).toUpperCase(); // COUPLE/MEETING/NORMAL

            int surcharge = seat.getSurchargePrice(); // ✅ int면 null체크 필요없음

            maxByType.merge(type, surcharge, Math::max);
        });

        List<SeatSurchargeResponse> out = new ArrayList<>();
        if (maxByType.containsKey("COUPLE")) out.add(new SeatSurchargeResponse("COUPLE", maxByType.get("COUPLE")));
        if (maxByType.containsKey("MEETING")) out.add(new SeatSurchargeResponse("MEETING", maxByType.get("MEETING")));

        return out;
    }
}
