package com.example.jeuxolympiques.service.impl;

import com.example.jeuxolympiques.model.Offer;
import com.example.jeuxolympiques.repository.OfferRepository;
import com.example.jeuxolympiques.service.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;

    @Autowired
    public OfferServiceImpl(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Offer> getAllOffers() {
        return offerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Offer getOfferById(Long id) {
        Offer offer = offerRepository.findByOfferId(id);
        if (offer == null) {
            throw new NoSuchElementException("Offre non trouvée avec l'ID: " + id);
        }
        return offer;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Offer> getOffersByType(String offerType) {
        if (offerType == null || offerType.trim().isEmpty()) {
            throw new IllegalArgumentException("Le type d'offre ne peut pas être vide");
        }
        return offerRepository.findByOfferType(offerType);
    }

    @Override
    public Offer saveOffer(Offer offer) {
        // Validation des données
        validateOffer(offer);
        return offerRepository.save(offer);
    }

    @Override
    public void deleteOffer(Long id) {
        Offer offer = offerRepository.findByOfferId(id);
        if (offer == null) {
            throw new NoSuchElementException("Impossible de supprimer: Offre non trouvée avec l'ID: " + id);
        }
        offerRepository.delete(offer);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return offerRepository.findByOfferId(id) != null;
    }

    // Méthodes supplémentaires utiles

    @Transactional(readOnly = true)
    public List<Offer> getAvailableOffers() {
        return offerRepository.findAll().stream()
                .filter(Offer::getIsAvailable)
                .toList();
    }

    @Transactional
    public Offer updateOffer(Long id, Offer offerDetails) {
        Offer existingOffer = getOfferById(id);

        // Mettre à jour les champs
        existingOffer.setName(offerDetails.getName());
        existingOffer.setDescription(offerDetails.getDescription());
        existingOffer.setPrice(offerDetails.getPrice());
        existingOffer.setOfferType(offerDetails.getOfferType());
        existingOffer.setPersonCount(offerDetails.getPersonCount());
        existingOffer.setIsAvailable(offerDetails.getIsAvailable());

        // Validation avant sauvegarde
        validateOffer(existingOffer);

        return offerRepository.save(existingOffer);
    }

    @Transactional
    public Offer updateOfferAvailability(Long id, boolean available) {
        Offer offer = getOfferById(id);
        offer.setIsAvailable(available);
        return offerRepository.save(offer);
    }

    // Méthode privée pour la validation
    private void validateOffer(Offer offer) {
        if (offer == null) {
            throw new IllegalArgumentException("L'offre ne peut pas être nulle");
        }

        if (offer.getName() == null || offer.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'offre est obligatoire");
        }

        if (offer.getPrice() == null || offer.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le prix doit être supérieur à zéro");
        }

        if (offer.getOfferType() == null || offer.getOfferType().trim().isEmpty()) {
            throw new IllegalArgumentException("Le type d'offre est obligatoire");
        }

        if (offer.getPersonCount() == null || offer.getPersonCount() < 1) {
            throw new IllegalArgumentException("Le nombre de personnes doit être au moins 1");
        }
    }
}