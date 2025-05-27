package com.example.vibetickets.service;

import com.example.vibetickets.model.Reservation;
import java.util.Optional;

public interface TicketVerificationService {

    /**
     * Vérifie la validité d'un billet à partir de sa clé finale
     * et marque le billet comme utilisé si valide.
     *
     * @param finalKey La clé finale à vérifier
     * @return La réservation si valide, sinon Optional vide
     */
    Optional<Reservation> verifyTicket(String finalKey);

    /**
     * Vérifie la validité d'un billet sans le marquer comme utilisé.
     * Utile pour pré-vérifications ou affichage d'informations.
     *
     * @param finalKey La clé finale à vérifier
     * @return La réservation si valide, sinon Optional vide
     */
    Optional<Reservation> checkTicketValidity(String finalKey);
}