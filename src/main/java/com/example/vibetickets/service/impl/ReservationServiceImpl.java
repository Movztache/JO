package com.example.vibetickets.service.impl;

import com.example.vibetickets.exception.PaymentException;
import com.example.vibetickets.model.Reservation;
import com.example.vibetickets.repository.ReservationRepository;
import com.example.vibetickets.model.Offer;
import com.example.vibetickets.model.UserApp;
import com.example.vibetickets.repository.OfferRepository;
import com.example.vibetickets.repository.UserAppRepository;

import com.example.vibetickets.service.PaymentService;
import com.example.vibetickets.service.ReservationService;
import com.example.vibetickets.service.UserAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserAppRepository userAppRepository;
    private final OfferRepository offerRepository;
    private final UserAppService userAppService;
    private final PaymentService paymentService; // Nouveau service de paiement

    @Autowired
    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            UserAppRepository userAppRepository,
            OfferRepository offerRepository,
            UserAppService userAppService,
            PaymentService paymentService) { // Ajout du service de paiement
        this.reservationRepository = reservationRepository;
        this.userAppRepository = userAppRepository;
        this.offerRepository = offerRepository;
        this.userAppService = userAppService;
        this.paymentService = paymentService;
    }

    /**
     * Recherche une réservation par son identifiant
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findById(id);
    }
    /**
     * Crée une nouvelle réservation de billet avec simulation de paiement.
     *
     * @param userAppId ID de l'utilisateur qui effectue la réservation
     * @param offerId ID de l'offre à réserver
     * @param quantity Nombre de billets à réserver
     * @param userKey Clé de l'utilisateur récupérée côté serveur
     * @param paymentInfo Informations de paiement au format "cardNumber|expiryDate|cvv"
     * @return L'objet Reservation créé
     * @throws NoSuchElementException si l'utilisateur ou l'offre n'existe pas
     * @throws PaymentException si le paiement échoue
     */
    @Override
    @Transactional
    public Reservation createTicketReservation(Long userAppId, Long offerId, int quantity, String userKey, String paymentInfo) {
        // Validation des paramètres d'entrée
        if (userAppId == null) {
            throw new IllegalArgumentException("userAppId ne peut pas être null");
        }

        if (offerId == null) {
            throw new IllegalArgumentException("offerId ne peut pas être null");
        }

        if (userKey == null || userKey.isEmpty()) {
            throw new IllegalArgumentException("userKey ne peut pas être null ou vide");
        }

        // Validation de la clé utilisateur
        if (!userAppService.validateUserKey(userAppId, userKey)) {
            throw new SecurityException("Clé utilisateur invalide");
        }

        // Récupération de l'utilisateur et de l'offre
        UserApp userApp = userAppRepository.findById(userAppId)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur non trouvé avec ID: " + userAppId));

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new NoSuchElementException("Offre non trouvée avec ID: " + offerId));

        if (!offer.getIsAvailable()) {
            throw new IllegalStateException("Offre non disponible");
        }

        // Création de la réservation
        Reservation reservation = new Reservation();
        reservation.setUserApp(userApp);
        reservation.setOffer(offer);
        reservation.setQuantity(quantity);
        reservation.setReservationDate(new Date());
        reservation.setStatus("PENDING");
        reservation.setIsUsed(false);

        // Génération d'une clé de réservation aléatoire
        String reservationKey = UUID.randomUUID().toString().substring(0, 8);
        reservation.setReservationKey(reservationKey);

        // Génération de la clé finale
        String finalKey = generateFinalKey(userKey, reservationKey, quantity);
        reservation.setFinalKey(finalKey);

        // Génération du QR Code
        String qrCode = reservationKey + "|" + userKey + "|" + quantity;
        reservation.setQrCode(qrCode);

        // Traitement du paiement
        String[] paymentDetails = paymentInfo.split("\\|");
        if (paymentDetails.length < 3) {
            throw new IllegalArgumentException("Informations de paiement invalides: format incorrect");
        }

        String cardNumber = paymentDetails[0];
        String expiryDate = paymentDetails[1];
        String cvv = paymentDetails[2];
        BigDecimal totalAmount = offer.getPrice().multiply(BigDecimal.valueOf(quantity));

        // Sauvegarder d'abord la réservation pour qu'elle ait un ID
        try {
            reservation = reservationRepository.save(reservation);

            // Traiter le paiement avec la réservation déjà sauvegardée
            boolean paymentSuccess = paymentService.processPayment(totalAmount, cardNumber, expiryDate, cvv, reservation);

            if (!paymentSuccess) {
                // Annuler la transaction en cas d'échec du paiement
                reservationRepository.delete(reservation);
                throw new PaymentException("Échec du traitement du paiement");
            }

            return reservation;
        } catch (Exception e) {
            // En cas d'erreur, essayer de supprimer la réservation si elle a été sauvegardée
            if (reservation.getReservationId() != null) {
                try {
                    reservationRepository.delete(reservation);
                } catch (Exception ex) {
                    // Ignorer les erreurs lors de la suppression
                }
            }
            throw e;
        }

    }

    /**
     * Génère une clé finale unique et sécurisée basée sur la clé utilisateur,
     * la clé de réservation et la quantité.
     * Cette clé est utilisée pour vérifier la validité du billet.
     *
     * @param userKey La clé personnelle de l'utilisateur
     * @param reservationKey La clé générée pour la réservation
     * @param quantity La quantité de billets réservés
     * @return Une clé finale unique et sécurisée
     */
    protected String generateFinalKey(String userKey, String reservationKey, int quantity) {
        // Combinaison des valeurs
        String combined = userKey + reservationKey + quantity;

        // Utilisation de SHA-256 pour générer une clé sécurisée
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));

            // Conversion en format hexadécimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            // Retourner une version raccourcie (32 premiers caractères) pour plus de praticité
            return hexString.substring(0, 32);
        } catch (NoSuchAlgorithmException e) {
            // En cas d'erreur, revenir à la méthode simple
            return userKey + reservationKey + quantity;
        }
    }
}