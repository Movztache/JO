package com.example.jeuxolympiques.service;

import com.example.jeuxolympiques.model.Reservation;
import com.example.jeuxolympiques.repository.ReservationRepository;
import com.example.jeuxolympiques.model.Offer;
import com.example.jeuxolympiques.model.UserApp;
import com.example.jeuxolympiques.repository.OfferRepository;
import com.example.jeuxolympiques.repository.UserAppRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserAppRepository userAppRepository;
    private final OfferRepository offerRepository;
    private final UserAppService userAppService; // Ajout du service utilisateur

    @Autowired
    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            UserAppRepository userAppRepository,
            OfferRepository offerRepository,
            UserAppService userAppService) {
        this.reservationRepository = reservationRepository;
        this.userAppRepository = userAppRepository;
        this.offerRepository = offerRepository;
        this.userAppService = userAppService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findById(id);
    }


    @Override
    @Transactional
    public Reservation createTicketReservation(Long userAppId, Long offerId, int quantity, String providedUserKey) {
        // Vérification que la clé fournie correspond à l'utilisateur
        if (!userAppService.validateUserKey(userAppId, providedUserKey)) {
            throw new SecurityException("Clé utilisateur invalide");
        }

        UserApp userApp = userAppRepository.findById(userAppId)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur non trouvé"));

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new NoSuchElementException("Offre non trouvée"));

        // Création de la réservation
        Reservation reservation = new Reservation();
        reservation.setUserApp(userApp);
        reservation.setOffer(offer);
        reservation.setQuantity(quantity);
        reservation.setQrCode(UUID.randomUUID().toString());
        reservation.setReservationDate(new Date());
        reservation.setReservationKey();

        // Génération de la clé finale sécurisée
        String finalKey = generateFinalKey(providedUserKey, reservation.getReservationKey(), quantity);
        reservation.setFinalKey(finalKey);
        reservation.setIsUsed(false);

        return reservationRepository.save(reservation);
    }

    protected String generateFinalKey(String userKey, String reservationKey, int quantity) {
        try {
            // Concaténation des informations pour créer la clé brute
            String rawKey = userKey + ":" + reservationKey + ":" + quantity;

            // Utilisation de l'algorithme SHA-256 pour le hachage
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawKey.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // Encodage en Base64 URL-safe sans padding pour une clé plus courte et utilisable dans les URL
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            // En cas d'erreur, fallback sur une méthode simple
            return userKey.substring(0, 8) + reservationKey + quantity;
        }
    }
    @Override
    @Transactional
    public Optional<Reservation> verifyTicket(String finalKey) {
        if (finalKey == null || finalKey.isEmpty()) {
            return Optional.empty();
        }

        // Recherche de la réservation par sa clé finale
        Optional<Reservation> reservationOpt = reservationRepository.findByFinalKey(finalKey);

        if (reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();

            // Si le billet n'a pas encore été utilisé
            if (!reservation.getIsUsed()) {
                try {
                    // Vérification du décryptage
                    String originalKey = reservation.getFinalKey();

                    // Comparer la clé fournie avec la clé stockée pour validation
                    if (originalKey.equals(finalKey)) {
                        // Marquer le billet comme utilisé
                        reservation.setIsUsed(true);
                        reservation.setUsageDate(new Date());

                        // Sauvegarder les modifications et retourner la réservation mise à jour
                        Reservation updatedReservation = reservationRepository.save(reservation);
                        return Optional.of(updatedReservation);
                    } else {
                        // Clé invalide
                        return Optional.empty();
                    }
                } catch (Exception e) {
                    // Erreur lors du décryptage
                    return Optional.empty();
                }
            } else {
                // Le billet a déjà été utilisé, retourner la réservation sans modification
                return reservationOpt;
            }
        }
        // Aucune réservation trouvée avec cette clé
        return Optional.empty();
    }

}