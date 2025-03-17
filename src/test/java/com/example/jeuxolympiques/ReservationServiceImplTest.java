package com.example.jeuxolympiques;

import com.example.jeuxolympiques.model.Offer;
import com.example.jeuxolympiques.model.Reservation;
import com.example.jeuxolympiques.model.UserApp;
import com.example.jeuxolympiques.repository.ReservationRepository;
import com.example.jeuxolympiques.service.ReservationServiceImpl;
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

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Reservation reservation1;
    private Reservation reservation2;
    private UserApp userApp;
    private Offer offer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Création d'un utilisateur de test
        userApp = new UserApp("Prénom", "Nom", "email@test.com", "Password1@");
        userApp.setUserId(1L);

        // Création d'une offre de test
        offer = new Offer();
        offer.setOfferId(1L);

        // Création d'une première réservation
        reservation1 = new Reservation();
        reservation1.setReservationId(1L);
        reservation1.setReservationDate(new Date());
        reservation1.setReservationKey("ABC12345");
        reservation1.setQrCode("QRCODE12345");
        reservation1.setQuantity(2);
        reservation1.setUserApp(userApp);
        reservation1.setOffer(offer);

        // Création d'une seconde réservation
        reservation2 = new Reservation();
        reservation2.setReservationId(2L);
        reservation2.setReservationDate(new Date());
        reservation2.setReservationKey("DEF67890");
        reservation2.setQrCode("QRCODE67890");
        reservation2.setQuantity(1);
        reservation2.setUserApp(userApp);
        reservation2.setOffer(offer);
    }

    @Test
    void findById_WithValidId_ShouldReturnReservation() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation1));

        // Act
        Optional<Reservation> result = reservationService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getReservationId());
        assertEquals("ABC12345", result.get().getReservationKey());
        assertEquals("QRCODE12345", result.get().getQrCode());
        assertEquals(2, result.get().getQuantity());
        assertEquals(userApp, result.get().getUserApp());
        assertEquals(offer, result.get().getOffer());
        verify(reservationRepository, times(1)).findById(1L);
    }

    @Test
    void findById_WithNullId_ShouldReturnEmptyOptional() {
        // Act
        Optional<Reservation> result = reservationService.findById(null);

        // Assert
        assertFalse(result.isPresent());
        verify(reservationRepository, never()).findById(any());
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmptyOptional() {
        // Arrange
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Reservation> result = reservationService.findById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(reservationRepository, times(1)).findById(999L);
    }

    @Test
    void findByUserAppId_WithValidId_ShouldReturnReservations() {
        // Arrange
        List<Reservation> reservations = Arrays.asList(reservation1, reservation2);
        when(reservationRepository.findByUserApp_UserId(1L)).thenReturn(reservations);

        // Act
        List<Reservation> result = reservationService.findByUserAppId(1L);

        // Assert
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getReservationId());
        assertEquals(2L, result.get(1).getReservationId());
        assertEquals("ABC12345", result.get(0).getReservationKey());
        assertEquals("DEF67890", result.get(1).getReservationKey());
        verify(reservationRepository, times(1)).findByUserApp_UserId(1L);
    }

    @Test
    void findByUserAppId_WithNullId_ShouldReturnEmptyList() {
        // Act
        List<Reservation> result = reservationService.findByUserAppId(null);

        // Assert
        assertTrue(result.isEmpty());
        verify(reservationRepository, never()).findByUserApp_UserId(any());
    }

    @Test
    void findByUserAppId_WithNonExistingId_ShouldReturnEmptyList() {
        // Arrange
        when(reservationRepository.findByUserApp_UserId(999L)).thenReturn(Collections.emptyList());

        // Act
        List<Reservation> result = reservationService.findByUserAppId(999L);

        // Assert
        assertTrue(result.isEmpty());
        verify(reservationRepository, times(1)).findByUserApp_UserId(999L);
    }
}