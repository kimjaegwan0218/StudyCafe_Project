package com.example.pentagon.repository;

import com.example.pentagon.domain.reservation.TimePrice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimePriceRepository extends JpaRepository<TimePrice, Integer> {
}