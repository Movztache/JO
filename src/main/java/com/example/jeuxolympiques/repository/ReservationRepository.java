package com.example.jeuxolympiques.repository;

import com.example.jeuxolympiques.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}