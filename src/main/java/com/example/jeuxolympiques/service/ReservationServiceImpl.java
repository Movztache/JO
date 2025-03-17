package com.example.jeuxolympiques.service;

import com.example.jeuxolympiques.model.Reservation;
import com.example.jeuxolympiques.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reservation> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return reservationRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findByUserAppId(Long userAppId) {
        if (userAppId == null) {
            return Collections.emptyList();
        }
        return reservationRepository.findByUserApp_UserId(userAppId);
    }


}