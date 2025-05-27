package com.example.vibetickets.service;

import com.example.vibetickets.model.Reservation;
import java.math.BigDecimal;

public interface PaymentService {
    /**
     * Traite un paiement pour une réservation.
     * Cette méthode simule le processus de paiement.
     *
     * @param amount Montant à payer
     * @param cardNumber Numéro de carte (simulé)
     * @param expiryDate Date d'expiration au format MM/YY
     * @param cvv Code de sécurité à 3 ou 4 chiffres
     * @param reservation Réservation associée au paiement
     * @return true si le paiement est accepté, false sinon
     */
    boolean processPayment(BigDecimal amount, String cardNumber, String expiryDate, String cvv, Reservation reservation);

    /**
     * Annule un paiement précédemment effectué.
     *
     * @param transactionId Identifiant unique de la transaction à annuler
     * @return true si l'annulation réussit, false sinon
     */
    boolean cancelPayment(String transactionId);

    /**
     * Génère un identifiant unique pour une transaction.
     *
     * @return Un identifiant de transaction unique
     */
    String generateTransactionId();
}