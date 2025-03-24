package com.example.jeuxolympiques.repository;

import com.example.jeuxolympiques.model.Reservation;
import org.springframework.lang.NonNull;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @NonNull
    Optional<Reservation> findById(@NonNull Long id);

    Optional<Reservation> findByFinalKey(String finalKey);


}