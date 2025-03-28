package com.example.jeuxolympiques;

import com.example.jeuxolympiques.dto.CartItemDTO;
import com.example.jeuxolympiques.model.Cart;
import com.example.jeuxolympiques.model.Offer;
import com.example.jeuxolympiques.model.UserApp;
import com.example.jeuxolympiques.repository.CartRepository;
import com.example.jeuxolympiques.repository.OfferRepository;
import com.example.jeuxolympiques.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OfferRepository offerRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private UserApp testUser;
    private Offer testOffer;
    private Cart testCart;
    private CartItemDTO testCartItemDTO;

    @BeforeEach
    void setUp() {
        // Initialisation de l'utilisateur de test avec email et mot de passe
        testUser = new UserApp();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");

        // Initialisation de l'offre de test
        testOffer = new Offer();
        testOffer.setOfferId(1L);
        testOffer.setName("Test Offer");
        testOffer.setDescription("Description de l'offre test");
        testOffer.setPrice(BigDecimal.valueOf(25.99));

        // Initialisation du panier de test
        testCart = new Cart();
        testCart.setCartId(1L);
        testCart.setQuantity(2);
        testCart.setUserApp(testUser);
        testCart.setOffer(testOffer);

        // Initialisation du DTO du panier
        testCartItemDTO = new CartItemDTO();
        testCartItemDTO.setOfferId(1L);
        testCartItemDTO.setQuantity(2);
    }

    @Test
    void getCartItems_ShouldReturnCartItemsList() {
        // Arrange
        List<Cart> cartList = new ArrayList<>();
        cartList.add(testCart);
        when(cartRepository.findByUserApp(testUser)).thenReturn(cartList);

        // Act
        List<CartItemDTO> result = cartService.getCartItems(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOffer.getOfferId(), result.get(0).getOfferId());
        assertEquals(testCart.getQuantity(), result.get(0).getQuantity());
        verify(cartRepository, times(1)).findByUserApp(testUser);
    }

    @Test
    void addToCart_NewItem_ShouldAddAndReturnCartItem() {
        // Arrange
        when(offerRepository.findById(testOffer.getOfferId())).thenReturn(Optional.of(testOffer));
        when(cartRepository.findByUserAppAndOffer(testUser, testOffer)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart savedCart = invocation.getArgument(0);
            savedCart.setCartId(1L);
            return savedCart;
        });

        // Act
        CartItemDTO result = cartService.addToCart(testUser, testCartItemDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testOffer.getOfferId(), result.getOfferId());
        assertEquals(testCartItemDTO.getQuantity(), result.getQuantity());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void addToCart_ExistingItem_ShouldUpdateQuantityAndReturnCartItem() {
        // Arrange
        when(offerRepository.findById(testOffer.getOfferId())).thenReturn(Optional.of(testOffer));
        when(cartRepository.findByUserAppAndOffer(testUser, testOffer)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        CartItemDTO result = cartService.addToCart(testUser, testCartItemDTO);

        // Assert
        assertNotNull(result);
        // La quantité devrait être mise à jour (2 de base + 2 ajoutés)
        assertEquals(4, result.getQuantity());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void updateCartItemQuantity_ShouldUpdateAndReturnCartItem() {
        // Arrange
        when(cartRepository.findById(testCart.getCartId())).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        CartItemDTO result = cartService.updateCartItemQuantity(testUser, testCart.getCartId(), 5);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getQuantity());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void updateCartItemQuantity_WrongUser_ShouldThrowException() {
        // Arrange
        UserApp anotherUser = new UserApp();
        anotherUser.setUserId(2L);
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("otherpass");
        testCart.setUserApp(anotherUser);  // Le panier appartient à un autre utilisateur

        when(cartRepository.findById(testCart.getCartId())).thenReturn(Optional.of(testCart));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                cartService.updateCartItemQuantity(testUser, testCart.getCartId(), 5)
        );
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void removeFromCart_ShouldDeleteAndReturnTrue() {
        // Arrange
        when(cartRepository.findById(testCart.getCartId())).thenReturn(Optional.of(testCart));

        // Si votre service utilise delete(cart) au lieu de deleteById(cartId)
        doNothing().when(cartRepository).delete(any(Cart.class));

        // Act
        boolean result = cartService.removeFromCart(testUser, testCart.getCartId());

        // Assert
        assertTrue(result);

        // Vérifiez que delete(cart) est appelé au lieu de deleteById(cartId)
        verify(cartRepository).delete(any(Cart.class));
    }

    @Test
    void removeFromCart_WrongUser_ShouldReturnFalse() {
        // Arrange
        UserApp anotherUser = new UserApp();
        anotherUser.setUserId(2L);
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("otherpass");
        testCart.setUserApp(anotherUser);  // Le panier appartient à un autre utilisateur

        when(cartRepository.findById(testCart.getCartId())).thenReturn(Optional.of(testCart));

        // Act & Assert
        // Au lieu de vérifier un retour false, attendez-vous à une exception
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                cartService.removeFromCart(testUser, testCart.getCartId())
        );

        // Vérifiez le message d'erreur
        assertEquals("Cet article du panier n'appartient pas à l'utilisateur spécifié", exception.getMessage());

        // Vérifiez que deleteById n'est jamais appelé
        verify(cartRepository, never()).deleteById(any());
        verify(cartRepository, never()).delete(any());
    }


    @Test
    void clearCart_ShouldDeleteAllUserItems() {
        // Arrange
        doNothing().when(cartRepository).deleteAllByUserApp(testUser);

        // Act
        cartService.clearCart(testUser);

        // Assert
        verify(cartRepository, times(1)).deleteAllByUserApp(testUser);
    }

    @Test
    void calculateCartTotal_ShouldReturnSumOfItems() {
        // Arrange
        List<Cart> cartList = new ArrayList<>();

        // Premier article
        cartList.add(testCart); // Prix 25.99 × quantité 2 = 51.98

        // Deuxième article
        Offer offer2 = new Offer();
        offer2.setOfferId(2L);
        offer2.setPrice(BigDecimal.valueOf(10.50)); // Modifié de 25.45 à 10.50

        Cart cart2 = new Cart();
        cart2.setCartId(2L);
        cart2.setOffer(offer2);
        cart2.setQuantity(3);
        cart2.setUserApp(testUser);

        cartList.add(cart2); // Prix 10.50 × quantité 3 = 31.50

        when(cartRepository.findByUserApp(testUser)).thenReturn(cartList);

        // Act
        BigDecimal result = cartService.calculateCartTotal(testUser);

        // Assert
        assertNotNull(result);
        assertTrue(
                result.subtract(new BigDecimal("83.48")).abs().compareTo(new BigDecimal("0.01")) < 0,
                "Le total du panier devrait être proche de 83.48"
        );
        verify(cartRepository, times(1)).findByUserApp(testUser);
    }

}