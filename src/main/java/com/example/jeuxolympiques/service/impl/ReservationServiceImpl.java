package com.example.jeuxolympiques.service.impl;

import com.example.jeuxolympiques.exception.PaymentException;
import com.example.jeuxolympiques.model.Reservation;
import com.example.jeuxolympiques.repository.ReservationRepository;
import com.example.jeuxolympiques.model.Offer;
import com.example.jeuxolympiques.model.UserApp;
import com.example.jeuxolympiques.repository.OfferRepository;
import com.example.jeuxolympiques.repository.UserAppRepository;

import com.example.jeuxolympiques.service.PaymentService;
import com.example.jeuxolympiques.service.ReservationService;
import com.example.jeuxolympiques.service.UserAppService;
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
     * @param providedUserKey Clé fournie par l'utilisateur pour vérification
     * @param paymentInfo Informations de paiement au format "cardNumber|expiryDate|cvv"
     * @return L'objet Reservation créé
     * @throws SecurityException si la clé utilisateur est invalide
     * @throws NoSuchElementException si l'utilisateur ou l'offre n'existe pas
     * @throws PaymentException si le paiement échoue
     */
    @Override
    @Transactional
    public Reservation createTicketReservation(Long userAppId, Long offerId, int quantity, String providedUserKey, String paymentInfo) {
        // Vérification que la clé fournie correspond à l'utilisateur
        if (!userAppService.validateUserKey(userAppId, providedUserKey)) {
            throw new SecurityException("Clé utilisateur invalide");
        }

        UserApp userApp = userAppRepository.findById(userAppId)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur non trouvé"));

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new NoSuchElementException("Offre non trouvée"));

        if (!offer.getIsAvailable()) {
            throw new IllegalStateException("Quantité insuffisante disponible");
        }

        // Création de la réservation
        Reservation reservation = new Reservation();
        reservation.setUserApp(userApp);
        reservation.setOffer(offer);
        reservation.setQuantity(quantity);
        reservation.setReservationDate(new Date());

        // Utilisation des nouvelles méthodes pour générer les clés automatiquement
        reservation.setReservationKey(); // Génère une clé de réservation aléatoire
        reservation.generateSecureQrCode(); // Génère le QR Code sécurisé avec le format requis
        reservation.setFinalKey(); // Génère la clé finale automatiquement
        reservation.setIsUsed(false);

        // Calculer le montant total à payer
        double totalAmount = offer.getPrice() * quantity;

        // Extraire les informations de paiement
        String[] paymentDetails = paymentInfo.split("\\|");
        if (paymentDetails.length < 3) {
            throw new IllegalArgumentException("Informations de paiement invalides");
        }

        String cardNumber = paymentDetails[0];
        String expiryDate = paymentDetails[1];
        String cvv = paymentDetails[2];

        // Traiter le paiement avec la réservation sauvegardée et un BigDecimal correct
        BigDecimal amount = BigDecimal.valueOf(totalAmount);
        boolean paymentSuccess = paymentService.processPayment(amount, cardNumber, expiryDate, cvv, reservation);

        if (!paymentSuccess) {
            // Annuler la transaction en cas d'échec du paiement
            reservationRepository.delete(reservation);
            throw new PaymentException("Échec du traitement du paiement");
        }
        reservation = reservationRepository.save(reservation);

        return reservation;
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