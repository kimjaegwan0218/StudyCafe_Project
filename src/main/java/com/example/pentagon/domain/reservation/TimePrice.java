package com.example.pentagon.domain.reservation;

import jakarta.persistence.*;

@Entity
@Table(name = "time_prices")
public class TimePrice {

    @Id
    @Column(name = "duration_hours")
    private Integer durationHours; // 1~5

    @Column(name = "price", nullable = false)
    private int price;

    public Integer getDurationHours() { return durationHours; }
    public int getPrice() { return price; }

    public void setDurationHours(Integer durationHours) { this.durationHours = durationHours; }
    public void setPrice(int price) { this.price = price; }
}
