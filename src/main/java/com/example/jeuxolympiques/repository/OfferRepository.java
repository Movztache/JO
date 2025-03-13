package com.example.jeuxolympiques.repository;

import com.example.jeuxolympiques.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferRepository extends JpaRepository<Offer, Long> {
}
