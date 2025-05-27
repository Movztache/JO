package com.example.vibetickets.repository;

import com.example.vibetickets.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {

    Offer findByOfferId(Long offerId);

    List<Offer> findByOfferType(String offerType);

}
