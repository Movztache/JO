package com.example.vibetickets.repository;

import com.example.vibetickets.model.Cart;
import com.example.vibetickets.model.Offer;
import com.example.vibetickets.model.UserApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // Trouver tous les éléments du panier d'un utilisateur
    List<Cart> findByUserApp(UserApp userApp);

    // Trouver un élément spécifique du panier (un utilisateur + une offre particulière)
    Optional<Cart> findByUserAppAndOffer(UserApp userApp, Offer offer);

    // Supprimer tous les éléments du panier d'un utilisateur
    void deleteAllByUserApp(UserApp userApp);
}