package com.example.jeuxolympiques.controller;

import com.example.jeuxolympiques.dto.CartItemDTO;
import com.example.jeuxolympiques.model.UserApp;
import com.example.jeuxolympiques.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Récupère le contenu du panier de l'utilisateur connecté
     */
    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getCart(Authentication authentication) {
        UserApp user = (UserApp) authentication.getPrincipal();
        List<CartItemDTO> cartItems = cartService.getCartItems(user);
        return ResponseEntity.ok(cartItems);
    }

    /**
     * Ajoute un article au panier
     */
    @PostMapping("/items")
    public ResponseEntity<CartItemDTO> addToCart(
            @RequestBody CartItemDTO cartItemDTO,
            Authentication authentication) {
        UserApp user = (UserApp) authentication.getPrincipal();
        CartItemDTO addedItem = cartService.addToCart(user, cartItemDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedItem);
    }

    /**
     * Met à jour la quantité d'un article dans le panier
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartItemDTO> updateCartItem(
            @PathVariable("itemId") Long cartId,
            @RequestParam Integer quantity,
            Authentication authentication) {
        UserApp user = (UserApp) authentication.getPrincipal();
        CartItemDTO updatedItem = cartService.updateCartItemQuantity(user, cartId, quantity);
        return ResponseEntity.ok(updatedItem);
    }

    /**
     * Supprime un article du panier
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Map<String, String>> removeFromCart(
            @PathVariable("itemId") Long cartId,
            Authentication authentication) {
        UserApp user = (UserApp) authentication.getPrincipal();
        boolean removed = cartService.removeFromCart(user, cartId);

        Map<String, String> response = new HashMap<>();
        if (removed) {
            response.put("message", "Article retiré du panier avec succès");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Article non trouvé dans le panier");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Vide complètement le panier de l'utilisateur
     */
    @DeleteMapping
    public ResponseEntity<Map<String, String>> clearCart(Authentication authentication) {
        UserApp user = (UserApp) authentication.getPrincipal();
        cartService.clearCart(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Panier vidé avec succès");
        return ResponseEntity.ok(response);
    }

    /**
     * Calcule le total du panier
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getCartSummary(Authentication authentication) {
        UserApp user = (UserApp) authentication.getPrincipal();
        Double total = cartService.calculateCartTotal(user);

        Map<String, Object> summary = new HashMap<>();
        summary.put("total", total);
        summary.put("itemCount", cartService.getCartItems(user).size());

        return ResponseEntity.ok(summary);
    }

    /**
     * Gère les exceptions liées au panier
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleCartException(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}