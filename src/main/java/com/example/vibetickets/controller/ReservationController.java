package com.example.vibetickets.controller;

import com.example.vibetickets.dto.ReservationCreateDTO;
import com.example.vibetickets.dto.ReservationResponseDTO;
import com.example.vibetickets.exception.PaymentException;
import com.example.vibetickets.model.Reservation;
import com.example.vibetickets.model.UserApp;
import com.example.vibetickets.service.ReservationService;
import com.example.vibetickets.service.UserAppService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Contrôleur REST pour la gestion des réservations
 */
@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReservationController {

    private final ReservationService reservationService;
    private final UserAppService userAppService;

    @Autowired
    public ReservationController(ReservationService reservationService, UserAppService userAppService) {
        this.reservationService = reservationService;
        this.userAppService = userAppService;
    }

    /**
     * Récupère une réservation par son ID
     * Accessible uniquement aux utilisateurs authentifiés
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservationResponseDTO> getReservationById(@PathVariable Long id) {
        try {
            Optional<Reservation> reservation = reservationService.findById(id);

            // Vérifier si la réservation existe
            if (reservation.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Vérifier si l'utilisateur est autorisé à voir cette réservation
            UserApp currentUser = getCurrentUser();
            if (currentUser == null || !reservation.get().getUserApp().getUserId().equals(currentUser.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(new ReservationResponseDTO(reservation.get()));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la récupération de la réservation", e);
        }
    }

    /**
     * Crée une nouvelle réservation
     * Accessible uniquement aux utilisateurs authentifiés
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createReservation(@Valid @RequestBody ReservationCreateDTO reservationDTO) {
        try {
            UserApp currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Récupérer le userKey directement depuis l'objet utilisateur authentifié
            String userKey = currentUser.getUserKey();
            if (userKey == null || userKey.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Erreur de configuration",
                                    "message", "La clé utilisateur n'est pas définie"));
            }

            // Créer la réservation en utilisant le userKey récupéré côté serveur
            Reservation reservation = reservationService.createTicketReservation(
                    currentUser.getUserId(),
                    reservationDTO.getOfferId(),
                    reservationDTO.getQuantity(),
                    userKey,  // Utiliser le userKey récupéré côté serveur
                    reservationDTO.getPaymentInfo()
            );

            // Créer la réponse avec les détails de la réservation
            ReservationResponseDTO responseDTO = new ReservationResponseDTO(reservation);

            // Retourner la réponse avec un message de succès incluant la clé finale
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Réservation créée avec succès");
            response.put("reservation", responseDTO);
            response.put("finalKey", responseDTO.getFinalKey());
            response.put("qrCode", responseDTO.getQrCode());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (SecurityException e) {
            // Erreur de sécurité (clé utilisateur invalide)
            Map<String, String> response = new HashMap<>();
            response.put("error", "Erreur de sécurité");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (NoSuchElementException e) {
            // Ressource non trouvée (utilisateur ou offre)
            Map<String, String> response = new HashMap<>();
            response.put("error", "Ressource non trouvée");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalStateException e) {
            // État invalide (offre non disponible)
            Map<String, String> response = new HashMap<>();
            response.put("error", "État invalide");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (PaymentException e) {
            // Erreur de paiement
            Map<String, String> response = new HashMap<>();
            response.put("error", "Erreur de paiement");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(response);
        } catch (Exception e) {
            // Autres erreurs
            Map<String, String> response = new HashMap<>();
            response.put("error", "Erreur interne du serveur");
            response.put("message", "Une erreur inattendue s'est produite lors de la création de la réservation");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Méthode utilitaire pour récupérer l'utilisateur connecté
     */
    private UserApp getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        return userAppService.findByEmail(email);
    }
}
