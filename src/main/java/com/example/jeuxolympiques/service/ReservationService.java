package com.example.jeuxolympiques.service;

import com.example.jeuxolympiques.model.Reservation;
import java.util.List;
import java.util.Optional;

public interface ReservationService {
    // Méthodes existantes
    Optional<Reservation> findById(Long id);


    Reservation createTicketReservation(Long userAppId, Long offerId, int quantity, String providedUserKey);

    // Méthode verifyTicket qui vérifie et marque comme utilisé en une seule opération
    Optional<Reservation> verifyTicket(String finalKey);

}
