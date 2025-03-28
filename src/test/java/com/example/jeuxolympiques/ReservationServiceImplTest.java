package com.example.jeuxolympiques;

import com.example.jeuxolympiques.model.Offer;
import com.example.jeuxolympiques.model.Reservation;
import com.example.jeuxolympiques.model.UserApp;
import com.example.jeuxolympiques.repository.OfferRepository;
import com.example.jeuxolympiques.repository.ReservationRepository;
import com.example.jeuxolympiques.repository.UserAppRepository;
import com.example.jeuxolympiques.service.PaymentService;
import com.example.jeuxolympiques.service.impl.ReservationServiceImpl;
import com.example.jeuxolympiques.service.UserAppService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserAppRepository userAppRepository;

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private UserAppService userAppService;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @Mock
    private PaymentService paymentService;


    private Reservation reservation1;
    private Reservation reservation2;
    private UserApp userApp;
    private Offer offer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userApp = new UserApp();
        userApp.setUserId(1L);
        userApp.setEmail("test@example.com");

        offer = new Offer();
        offer.setOfferId(1L);
        // Supposons que l'offre a des places disponibles

        reservation1 = new Reservation();
        reservation1.setReservationId(1L);
        reservation1.setUserApp(userApp);
        reservation1.setOffer(offer);
        reservation1.setQuantity(2);
        reservation1.setReservationDate(new Date());
        reservation1.setReservationKey("key123");
        reservation1.setFinalKey("final123");
        reservation1.setIsUsed(false);

        reservation2 = new Reservation();
        reservation2.setReservationId(2L);
        reservation2.setUserApp(userApp);
        reservation2.setOffer(offer);
        reservation2.setQuantity(1);
        reservation2.setReservationDate(new Date());
        reservation2.setReservationKey("key456");
        reservation2.setFinalKey("final456");
        reservation2.setIsUsed(true);
    }

    @Test
    void findById_WithValidId_ShouldReturnReservation() {
        // Given
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation1));

        // When
        Optional<Reservation> result = reservationService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getReservationId());
        verify(reservationRepository).findById(1L);
    }

    @Test
    void findById_WithInvalidId_ShouldReturnEmptyOptional() {
        // Given
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Reservation> result = reservationService.findById(99L);

        // Then
        assertFalse(result.isPresent());
        verify(reservationRepository).findById(99L);
    }

    @Test
    void createTicketReservation_WithValidData_ShouldCreateReservation() {
        // Given
        Long userId = 1L;
        Long offerId = 1L;
        int quantity = 2;
        String providedUserKey = "valid-key";
        String paymentInfo = "1234567890123456|12/25|123";

        // 1. Configuration du mock pour valider la clé utilisateur
        when(userAppService.validateUserKey(userId, providedUserKey)).thenReturn(true);

        // 2. Configuration de l'utilisateur
        UserApp userApp = new UserApp();
        userApp.setUserId(userId);
        when(userAppRepository.findById(userId)).thenReturn(Optional.of(userApp));

        // 3. Configuration de l'offre
        Offer offer = new Offer();
        offer.setOfferId(offerId);
        offer.setIsAvailable(true); // Assurez-vous que l'offre est disponible
        offer.setPrice(BigDecimal.valueOf(100));
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(offer));

        // 4. Configuration du processeur de paiement pour renvoyer true
        when(paymentService.processPayment(
                any(BigDecimal.class),
                anyString(),
                anyString(),
                anyString(),
                any(Reservation.class)
        )).thenReturn(true);

        // 5. Configuration pour retourner la réservation sauvegardée
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation savedRes = invocation.getArgument(0);
            savedRes.setReservationId(999L);
            return savedRes;
        });

        // When
        Reservation result = reservationService.createTicketReservation(
                userId, offerId, quantity, providedUserKey, paymentInfo);

        // Then
        assertNotNull(result);
        assertEquals(999L, result.getReservationId());
        assertEquals(userId, result.getUserApp().getUserId());
        assertEquals(offerId, result.getOffer().getOfferId());
        assertEquals(quantity, result.getQuantity());

        // Vérification des interactions
        verify(userAppService).validateUserKey(userId, providedUserKey);
        verify(userAppRepository).findById(userId);
        verify(offerRepository).findById(offerId);
        verify(paymentService).processPayment(
                eq(offer.getPrice().multiply(BigDecimal.valueOf(quantity))),
                anyString(),
                anyString(),
                anyString(),
                any(Reservation.class)
        );
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createTicketReservation_WithInvalidUserId_ShouldThrowException() {
        // Given
        Long userId = 99L;
        Long offerId = 1L;
        int quantity = 2;
        String providedUserKey = "valid-key";
        String paymentInfo = "1234567890123456|12/25|123";

        // Configurer le userAppService pour valider la clé utilisateur
        when(userAppService.validateUserKey(userId, providedUserKey)).thenReturn(true);

        // Simuler un utilisateur non trouvé
        when(userAppRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () ->
                reservationService.createTicketReservation(userId, offerId, quantity, providedUserKey, paymentInfo));

        assertEquals("Utilisateur non trouvé", exception.getMessage());

        // Vérifier que les méthodes attendues ont été appelées
        verify(userAppService).validateUserKey(userId, providedUserKey);
        verify(userAppRepository).findById(userId);
    }


    @Test
    void createTicketReservation_WithInvalidOfferId_ShouldThrowException() {
        // Given
        Long userId = 1L;
        Long offerId = 99L;
        int quantity = 2;
        String providedUserKey = "valid-key";
        String paymentInfo = "1234567890123456|12/25|123";

        // Configurer le userAppService pour valider la clé utilisateur
        when(userAppService.validateUserKey(userId, providedUserKey)).thenReturn(true);

        // Configurer l'utilisateur
        UserApp userApp = new UserApp();
        userApp.setUserId(userId);
        when(userAppRepository.findById(userId)).thenReturn(Optional.of(userApp));

        // Simuler une offre non trouvée
        when(offerRepository.findById(offerId)).thenReturn(Optional.empty());

        // When & Then
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () ->
                reservationService.createTicketReservation(userId, offerId, quantity, providedUserKey, paymentInfo));

        assertEquals("Offre non trouvée", exception.getMessage());

        // Vérifier que les méthodes attendues ont été appelées
        verify(userAppService).validateUserKey(userId, providedUserKey);
        verify(userAppRepository).findById(userId);
        verify(offerRepository).findById(offerId);
    }

    @Test
    void createTicketReservation_WithInvalidUserKey_ShouldThrowException() {
        // Given
        Long userId = 1L;
        Long offerId = 1L;
        int quantity = 2;
        String invalidUserKey = "invalid-key";
        String paymentInfo = "1234567890123456|12/25|123";

        // Configurer le userAppService pour rejeter la clé utilisateur
        when(userAppService.validateUserKey(userId, invalidUserKey)).thenReturn(false);

        // When & Then
        SecurityException exception = assertThrows(SecurityException.class, () ->
                reservationService.createTicketReservation(userId, offerId, quantity, invalidUserKey, paymentInfo));

        assertEquals("Clé utilisateur invalide", exception.getMessage());

        // Vérifier que seule la validation de la clé a été tentée
        verify(userAppService).validateUserKey(userId, invalidUserKey);
        verifyNoInteractions(userAppRepository);
        verifyNoInteractions(offerRepository);
    }

    @Test
    void createTicketReservation_WithInsufficientQuantity_ShouldThrowException() {
        // Given
        Long userId = 1L;
        Long offerId = 1L;
        int quantity = 1;
        String providedUserKey = "valid-key";
        String paymentInfo = "1234567890123456|12/25|123";

        // Configuration des mocks
        when(userAppService.validateUserKey(userId, providedUserKey)).thenReturn(true);

        // Créer un utilisateur valide
        UserApp userApp = new UserApp();
        userApp.setUserId(userId);
        when(userAppRepository.findById(userId)).thenReturn(Optional.of(userApp));

        // Créer une offre non disponible (isAvailable = false)
        Offer offer = new Offer();
        offer.setOfferId(offerId);
        offer.setIsAvailable(false); // L'offre n'est pas disponible
        offer.setPrice(BigDecimal.valueOf(100.0));
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(offer));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                reservationService.createTicketReservation(userId, offerId, quantity, providedUserKey, paymentInfo));

        assertEquals("Quantité insuffisante disponible", exception.getMessage());

        verify(userAppService).validateUserKey(userId, providedUserKey);
        verify(userAppRepository).findById(userId);
        verify(offerRepository).findById(offerId);

        // Assurez-vous que le service de paiement n'a pas été appelé
        verifyNoInteractions(paymentService);
    }
}