package com.example.jeuxolympiques.service;

import com.example.jeuxolympiques.model.Reservation;
import java.util.Optional;

public interface ReservationService {
    /**
     * Recherche une réservation par son identifiant
     */
    Optional<Reservation> findById(Long id);

    /**
     * Crée une nouvelle réservation de billet avec paiement
     *
     * @param userAppId ID de l'utilisateur qui effectue la réservation
     * @param offerId ID de l'offre à réserver
     * @param quantity Nombre de billets à réserver
     * @param providedUserKey Clé fournie par l'utilisateur pour vérification
     * @param paymentInfo Informations de paiement au format "cardNumber|expiryDate|cvv"
     * @return L'objet Reservation créé
     */
    Reservation createTicketReservation(Long userAppId, Long offerId, int quantity, String providedUserKey, String paymentInfo);

}