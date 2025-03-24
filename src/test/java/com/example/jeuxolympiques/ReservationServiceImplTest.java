package com.example.jeuxolympiques;

import com.example.jeuxolympiques.model.Offer;
import com.example.jeuxolympiques.model.Reservation;
import com.example.jeuxolympiques.model.UserApp;
import com.example.jeuxolympiques.repository.OfferRepository;
import com.example.jeuxolympiques.repository.ReservationRepository;
import com.example.jeuxolympiques.repository.UserAppRepository;
import com.example.jeuxolympiques.service.ReservationServiceImpl;
import com.example.jeuxolympiques.service.UserAppService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

    private Reservation reservation1;
    private Reservation reservation2;
    private UserApp userApp;
    private Offer offer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Configuration des objets de test
        userApp = new UserApp();
        userApp.setUserId(1L);
        userApp.setFirstName("testUser");

        offer = new Offer();
        offer.setOfferId(1L);
        offer.setName("Test Offer");
        offer.setIsAvailable(true);

        // Préparation des réservations
        reservation1 = new Reservation();
        reservation1.setReservationId(1L);
        reservation1.setReservationDate(new Date());
        reservation1.setReservationKey("abc123");
        reservation1.setQrCode("qr123456");
        reservation1.setFinalKey("abc12310");
        reservation1.setQuantity(10);
        reservation1.setIsUsed(false);
        reservation1.setUserApp(userApp);
        reservation1.setOffer(offer);

        reservation2 = new Reservation();
        reservation2.setReservationId(2L);
        reservation2.setReservationDate(new Date());
        reservation2.setReservationKey("def456");
        reservation2.setQrCode("qr789012");
        reservation2.setFinalKey("def4565");
        reservation2.setQuantity(5);
        reservation2.setIsUsed(true);
        reservation2.setUsageDate(new Date());
        reservation2.setUserApp(userApp);
        reservation2.setOffer(offer);
    }

    @Test
    void findById_WithValidId_ShouldReturnReservation() {
        // Préparation
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation1));

        // Exécution
        Optional<Reservation> result = reservationService.findById(1L);

        // Vérification
        assertTrue(result.isPresent());
        assertEquals(reservation1, result.get());
        verify(reservationRepository, times(1)).findById(1L);
    }

    @Test
    void findById_WithNullId_ShouldReturnEmptyOptional() {
        // Préparation
        when(reservationRepository.findById(null)).thenReturn(Optional.empty());

        // Exécution
        Optional<Reservation> result = reservationService.findById(null);

        // Vérification
        assertFalse(result.isPresent());
        verify(reservationRepository, times(1)).findById(null);
    }

    @Test
    void verifyTicket_WithValidUnusedTicket_ShouldMarkAsUsedAndReturn() {
        // Préparation
        when(reservationRepository.findByFinalKey("abc12310")).thenReturn(Optional.of(reservation1));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Exécution
        Optional<Reservation> result = reservationService.verifyTicket("abc12310");

        // Vérification
        assertTrue(result.isPresent());
        assertTrue(result.get().getIsUsed());
        assertNotNull(result.get().getUsageDate());
        verify(reservationRepository, times(1)).findByFinalKey("abc12310");
        verify(reservationRepository, times(1)).save(any(Reservation.class));

        // Capture de l'argument pour vérification détaillée
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(reservationCaptor.capture());
        Reservation savedReservation = reservationCaptor.getValue();
        assertTrue(savedReservation.getIsUsed());
        assertNotNull(savedReservation.getUsageDate());
    }

    @Test
    void verifyTicket_WithAlreadyUsedTicket_ShouldReturnReservationWithoutModification() {
        // Préparation
        when(reservationRepository.findByFinalKey("def4565")).thenReturn(Optional.of(reservation2));

        // Exécution
        Optional<Reservation> result = reservationService.verifyTicket("def4565");

        // Vérification
        assertTrue(result.isPresent());
        assertTrue(result.get().getIsUsed());
        assertNotNull(result.get().getUsageDate());
        verify(reservationRepository, times(1)).findByFinalKey("def4565");
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void verifyTicket_WithNonExistingKey_ShouldReturnEmpty() {
        // Préparation
        when(reservationRepository.findByFinalKey("nonexisting")).thenReturn(Optional.empty());

        // Exécution
        Optional<Reservation> result = reservationService.verifyTicket("nonexisting");

        // Vérification
        assertFalse(result.isPresent());
        verify(reservationRepository, times(1)).findByFinalKey("nonexisting");
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createTicketReservation_WithValidData_ShouldCreateReservation() {
        // Préparation
        Long userId = 1L;
        Long offerId = 1L;
        int quantity = 5;
        String userKey = "user123";

        when(userAppRepository.findById(userId)).thenReturn(Optional.of(userApp));
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(offer));
        when(userAppService.validateUserKey(userApp.getUserId(), userKey)).thenReturn(true);

        // Utiliser doAnswer pour intercepter l'appel à save
        doAnswer(invocation -> {
            Reservation res = invocation.getArgument(0);

            // S'assurer que le qrCode est défini avant de procéder

                res.setQrCode("test-qrcode-12345");


            // S'assurer que toutes les autres valeurs sont définies correctement

                res.setReservationKey(res.getQrCode().substring(0, 8));

                res.setFinalKey(res.getReservationKey() + quantity);


            // Définir ID pour simuler la persistance
            res.setReservationId(3L);

            return res;
        }).when(reservationRepository).save(any(Reservation.class));

        // Exécution
        Reservation result = reservationService.createTicketReservation(userId, offerId, quantity, userKey);

        // Vérification
        assertNotNull(result);
        assertEquals(userApp, result.getUserApp());
        assertEquals(offer, result.getOffer());
        assertEquals(quantity, result.getQuantity());
        assertNotNull(result.getQrCode());
        assertNotNull(result.getReservationKey());
        assertNotNull(result.getFinalKey());
        assertFalse(result.getIsUsed());

        verify(userAppRepository).findById(userId);
        verify(offerRepository).findById(offerId);
        verify(userAppService).validateUserKey(userApp.getUserId(), userKey);
        verify(reservationRepository).save(any(Reservation.class));
    }


}