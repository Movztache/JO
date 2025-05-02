package com.example.jeuxolympiques.controller;

import com.example.jeuxolympiques.model.Reservation;
import com.example.jeuxolympiques.service.TicketVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
public class TicketVerificationController {

    private final TicketVerificationService ticketVerificationService;

    @Autowired
    public TicketVerificationController(TicketVerificationService ticketVerificationService) {
        this.ticketVerificationService = ticketVerificationService;
    }

    /**
     * Vérifie et valide un billet, le marquant comme utilisé si valide.
     * @param finalKey Clé de vérification du billet
     * @return Détails de la réservation si valide ou message d'erreur
     */
    @PostMapping("/verify/{finalKey}")
    public ResponseEntity<?> verifyTicket(@PathVariable String finalKey) {
        Optional<Reservation> result = ticketVerificationService.verifyTicket(finalKey);
        return createResponse(result);
    }

    /**
     * Vérifie la validité d'un billet sans le marquer comme utilisé.
     * @param finalKey Clé de vérification du billet
     * @return Détails de la réservation si valide ou message d'erreur
     */
    @GetMapping("/check/{finalKey}")
    public ResponseEntity<?> checkTicket(@PathVariable String finalKey) {
        Optional<Reservation> result = ticketVerificationService.checkTicketValidity(finalKey);
        return createResponse(result);
    }

    /**
     * Méthode utilitaire pour créer une réponse standardisée
     * @param reservation Réservation optionnelle
     * @return Réponse HTTP appropriée
     */
    private ResponseEntity<?> createResponse(Optional<Reservation> reservation) {
        if (reservation.isPresent()) {
            return ResponseEntity.ok(reservation.get());
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Billet invalide ou déjà utilisé");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}