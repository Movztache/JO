package com.example.vibetickets.service;

import com.example.vibetickets.model.Reservation;
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
     * @param userKey Clé de l'utilisateur récupérée côté serveur
     * @param paymentInfo Informations de paiement au format "cardNumber|expiryDate|cvv"
     * @return L'objet Reservation créé
     */
    Reservation createTicketReservation(Long userAppId, Long offerId, int quantity, String userKey, String paymentInfo);

}