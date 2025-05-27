package com.example.vibetickets.controller;

import com.example.vibetickets.dto.CartItemDTO;
import com.example.vibetickets.model.UserApp;
import com.example.vibetickets.service.CartService;
import com.example.vibetickets.service.UserAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CartController {

    private final CartService cartService;
    private final UserAppService userAppService;

    @Autowired
    public CartController(CartService cartService, UserAppService userAppService) {
        this.cartService = cartService;
        this.userAppService = userAppService;
    }

    /**
     * Méthode utilitaire pour récupérer l'utilisateur connecté
     */
    private UserApp getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userAppService.findByEmail(email);
    }

    /**
     * Récupère le contenu du panier de l'utilisateur connecté
     */
    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getCart() {
        try {
            UserApp user = getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            List<CartItemDTO> cart = cartService.getCartItems(user);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Ajoute un article au panier
     */
    @PostMapping("/items")
    public ResponseEntity<CartItemDTO> addToCart(@RequestBody CartItemDTO cartItemDTO) {
        try {
            UserApp user = getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            CartItemDTO savedItem = cartService.addToCart(user, cartItemDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Met à jour la quantité d'un article dans le panier
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartItemDTO> updateCartItem(
            @PathVariable("itemId") Long cartId,
            @RequestParam Integer quantity) {
        try {
            UserApp user = getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            CartItemDTO updatedItem = cartService.updateCartItemQuantity(user, cartId, quantity);
            return ResponseEntity.ok(updatedItem);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Supprime un article du panier
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Map<String, String>> removeFromCart(@PathVariable("itemId") Long cartId) {
        try {
            UserApp user = getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            boolean removed = cartService.removeFromCart(user, cartId);

            Map<String, String> response = new HashMap<>();
            if (removed) {
                response.put("message", "Article supprimé du panier avec succès");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Impossible de supprimer l'article");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Vide complètement le panier de l'utilisateur
     */
    @DeleteMapping
    public ResponseEntity<Map<String, String>> clearCart() {
        try {
            UserApp user = getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            cartService.clearCart(user);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Panier vidé avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Calcule le total du panier
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getCartSummary() {
        try {
            UserApp user = getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            List<CartItemDTO> cartItems = cartService.getCartItems(user);
            BigDecimal total = cartService.calculateCartTotal(user);

            int itemCount = cartItems.stream()
                    .mapToInt(CartItemDTO::getQuantity)
                    .sum();

            Map<String, Object> summary = new HashMap<>();
            summary.put("total", total);
            summary.put("itemCount", itemCount);
            summary.put("items", cartItems);

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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