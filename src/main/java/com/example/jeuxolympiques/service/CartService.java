package com.example.jeuxolympiques.service;

import com.example.jeuxolympiques.dto.CartItemDTO;
import com.example.jeuxolympiques.model.UserApp;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {

    /**
     * Récupère tous les articles du panier d'un utilisateur
     * @param userApp L'utilisateur concerné
     * @return Liste des articles dans le panier
     */
    List<CartItemDTO> getCartItems(UserApp userApp);

    /**
     * Ajoute un article au panier
     * @param userApp L'utilisateur concerné
     * @param cartItemDTO L'article à ajouter
     * @return L'article ajouté avec son ID mis à jour
     */
    CartItemDTO addToCart(UserApp userApp, CartItemDTO cartItemDTO);

    /**
     * Met à jour la quantité d'un article dans le panier
     * @param userApp L'utilisateur concerné
     * @param cartId L'ID de l'article dans le panier
     * @param quantity La nouvelle quantité
     * @return L'article mis à jour
     */
    CartItemDTO updateCartItemQuantity(UserApp userApp, Long cartId, Integer quantity);

    /**
     * Supprime un article du panier
     * @param userApp L'utilisateur concerné
     * @param cartId L'ID de l'article à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    boolean removeFromCart(UserApp userApp, Long cartId);

    /**
     * Vide complètement le panier d'un utilisateur
     * @param userApp L'utilisateur concerné
     */
    void clearCart(UserApp userApp);

    /**
     * Calcule le total du panier d'un utilisateur
     * @param userApp L'utilisateur concerné
     * @return Le montant total du panier
     */
    BigDecimal calculateCartTotal(UserApp userApp);
}