package com.example.vibetickets.service;

import com.example.vibetickets.model.Offer;
import java.util.List;


public interface OfferService {
    List<Offer> getAllOffers();
    Offer getOfferById(Long id);
    List<Offer> getOffersByType(String offerType);
    Offer saveOffer(Offer offer);
    void deleteOffer(Long id);
    boolean existsById(Long id);
    Offer updateOffer(Long id, Offer offerDetails);
    Offer updateOfferAvailability(Long id, boolean available);
}