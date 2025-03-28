package com.example.jeuxolympiques.service.impl;

import com.example.jeuxolympiques.model.Reservation;
import com.example.jeuxolympiques.repository.ReservationRepository;
import com.example.jeuxolympiques.service.TicketVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
public class TicketVerificationServiceImpl implements TicketVerificationService {

    private final ReservationRepository reservationRepository;

    @Autowired
    public TicketVerificationServiceImpl(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    /**
     * Vérifie la validité d'un billet et le marque comme utilisé si valide.
     */
    @Override
    @Transactional
    public Optional<Reservation> verifyTicket(String finalKey) {
        Optional<Reservation> reservationOpt = reservationRepository.findByFinalKey(finalKey);

        if (reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();

            if (!reservation.getIsUsed()) {
                reservation.setIsUsed(true);
                // Utiliser setUsageDate au lieu de setUsedDate
                reservation.setUsageDate(new Date());
                reservationRepository.save(reservation);
                return Optional.of(reservation);
            }
        }

        return Optional.empty();
    }

    /**
     * Vérifie simplement la validité d'un billet sans le marquer comme utilisé.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Reservation> checkTicketValidity(String finalKey) {
        Optional<Reservation> reservationOpt = reservationRepository.findByFinalKey(finalKey);

        if (reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();

            // Retourner la réservation seulement si elle n'est pas utilisée
            if (!reservation.getIsUsed()) {
                return reservationOpt;
            }
        }

        return Optional.empty();
    }
}