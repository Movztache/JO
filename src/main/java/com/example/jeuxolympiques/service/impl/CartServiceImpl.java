package com.example.jeuxolympiques.service.impl;

import com.example.jeuxolympiques.dto.CartItemDTO;
import com.example.jeuxolympiques.model.Cart;
import com.example.jeuxolympiques.model.Offer;
import com.example.jeuxolympiques.model.UserApp;
import com.example.jeuxolympiques.repository.CartRepository;
import com.example.jeuxolympiques.repository.OfferRepository;
import com.example.jeuxolympiques.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final OfferRepository offerRepository;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, OfferRepository offerRepository) {
        this.cartRepository = cartRepository;
        this.offerRepository = offerRepository;
    }

    @Override
    public List<CartItemDTO> getCartItems(UserApp userApp) {
        List<Cart> cartItems = cartRepository.findByUserApp(userApp);

        return cartItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CartItemDTO addToCart(UserApp userApp, CartItemDTO cartItemDTO) {
        // Vérifier si l'offre existe
        Offer offer = offerRepository.findById(cartItemDTO.getOfferId())
                .orElseThrow(() -> new IllegalArgumentException("Offre non trouvée avec l'ID: " + cartItemDTO.getOfferId()));

        // Vérifier si l'article existe déjà dans le panier
        Optional<Cart> existingCartItem = cartRepository.findByUserAppAndOffer(userApp, offer);

        Cart cart;
        if (existingCartItem.isPresent()) {
            // Mettre à jour la quantité si l'article existe déjà
            cart = existingCartItem.get();
            cart.setQuantity(cart.getQuantity() + cartItemDTO.getQuantity());
        } else {
            // Créer un nouvel article si l'article n'existe pas
            cart = new Cart();
            cart.setUserApp(userApp);
            cart.setOffer(offer);
            cart.setQuantity(cartItemDTO.getQuantity());
        }

        // Sauvegarder l'article dans le panier
        cart = cartRepository.save(cart);

        return convertToDTO(cart);
    }

    @Override
    @Transactional
    public CartItemDTO updateCartItemQuantity(UserApp userApp, Long cartId, Integer quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Article du panier non trouvé avec l'ID: " + cartId));

        // Vérifier que l'article appartient bien à l'utilisateur
        if (!cart.getUserApp().equals(userApp)) {
            throw new IllegalArgumentException("Cet article du panier n'appartient pas à l'utilisateur spécifié");
        }

        cart.setQuantity(quantity);
        cart = cartRepository.save(cart);

        return convertToDTO(cart);
    }

    @Override
    @Transactional
    public boolean removeFromCart(UserApp userApp, Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Article du panier non trouvé avec l'ID: " + cartId));

        // Vérifier que l'article appartient bien à l'utilisateur
        if (!cart.getUserApp().equals(userApp)) {
            throw new IllegalArgumentException("Cet article du panier n'appartient pas à l'utilisateur spécifié");
        }

        cartRepository.delete(cart);
        return true;
    }

    @Override
    @Transactional
    public void clearCart(UserApp userApp) {
        cartRepository.deleteAllByUserApp(userApp);
    }

    @Override
    public Double calculateCartTotal(UserApp userApp) {
        List<CartItemDTO> cartItems = getCartItems(userApp);
        return cartItems.stream()
                .map(item -> item.getTotalPrice() != null ? item.getTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();
    }

    /**
     * Convertit un objet Cart en CartItemDTO
     */
    private CartItemDTO convertToDTO(Cart cart) {
        CartItemDTO dto = new CartItemDTO();
        dto.setCartId(cart.getCartId());
        dto.setOfferId(cart.getOffer().getOfferId());
        dto.setQuantity(cart.getQuantity());
        dto.setOfferName(cart.getOffer().getName());

        // Convertir Double en BigDecimal
        if (cart.getOffer().getPrice() != null) {
            BigDecimal price = BigDecimal.valueOf(cart.getOffer().getPrice());
            dto.setOfferPrice(price);
            // Calculer le prix total (prix unitaire × quantité)
            BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(cart.getQuantity()));
            dto.setTotalPrice(totalPrice);
        } else {
            dto.setOfferPrice(BigDecimal.ZERO);
            dto.setTotalPrice(BigDecimal.ZERO);
        }
        return dto;
    }

}