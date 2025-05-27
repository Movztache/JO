package com.example.vibetickets.controller;

import com.example.vibetickets.model.Offer;
import com.example.vibetickets.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/offers")
public class OfferController {

    private final OfferService offerService;

    @Autowired
    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    /**
     * Récupère toutes les offres disponibles
     * Accessible à tous les utilisateurs
     */
    @GetMapping
    public ResponseEntity<List<Offer>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAllOffers());
    }

    /**
     * Récupère une offre par son ID
     * Accessible à tous les utilisateurs
     */
    @GetMapping("/{id}")
    public ResponseEntity<Offer> getOfferById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(offerService.getOfferById(id));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre non trouvée", e);
        }
    }

    /**
     * Récupère les offres par type
     * Accessible à tous les utilisateurs
     */
    @GetMapping("/type/{offerType}")
    public ResponseEntity<List<Offer>> getOffersByType(@PathVariable String offerType) {
        return ResponseEntity.ok(offerService.getOffersByType(offerType));
    }

    /**
     * Crée une nouvelle offre
     * Réservé aux Administrateurs
     */
    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Offer> createOffer(@Valid @RequestBody Offer offer) {
        return new ResponseEntity<>(offerService.saveOffer(offer), HttpStatus.CREATED);
    }

    /**
     * Met à jour une offre existante
     * Réservé aux Administrateurs
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Offer> updateOffer(@PathVariable Long id, @Valid @RequestBody Offer offerDetails) {
        try {
            return ResponseEntity.ok(offerService.updateOffer(id, offerDetails));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre non trouvée", e);
        }
    }

    /**
     * Met à jour la disponibilité d'une offre
     * Réservé aux Administrateurs
     */
    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Offer> updateOfferAvailability(@PathVariable Long id, @RequestParam boolean available) {
        try {
            return ResponseEntity.ok(offerService.updateOfferAvailability(id, available));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre non trouvée", e);
        }
    }

    /**
     * Supprime une offre
     * Réservé aux Administrateurs
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        try {
            offerService.deleteOffer(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre non trouvée", e);
        }
    }
}