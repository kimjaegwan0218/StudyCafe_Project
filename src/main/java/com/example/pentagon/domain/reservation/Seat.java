package com.example.pentagon.domain.reservation;

import com.example.pentagon.domain.enums.SeatType;
import jakarta.persistence.*;

@Entity
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long seatId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private SeatType type;

    @Column(name = "surcharge_price", nullable = false)
    private int surchargePrice;

    @Column(name = "active")
    private boolean active;

    public Long getSeatId() { return seatId; }
    public SeatType getType() { return type; }
    public int getSurchargePrice() { return surchargePrice; }
    public boolean isActive() { return active; }

    public void setType(SeatType type) { this.type = type; }
    public void setSurchargePrice(int surchargePrice) { this.surchargePrice = surchargePrice; }
    public void setActive(boolean active) { this.active = active; }
}
