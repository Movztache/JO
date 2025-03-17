package com.example.jeuxolympiques.service;

import com.example.jeuxolympiques.model.Reservation;
import java.util.List;
import java.util.Optional;

public interface ReservationService {

    Optional<Reservation> findById(Long id);

    List<Reservation> findByUserAppId(Long userAppId);

}