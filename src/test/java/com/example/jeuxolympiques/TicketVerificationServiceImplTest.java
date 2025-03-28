package com.example.jeuxolympiques;

import com.example.jeuxolympiques.model.Reservation;
import com.example.jeuxolympiques.repository.ReservationRepository;
import com.example.jeuxolympiques.service.impl.TicketVerificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketVerificationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private TicketVerificationServiceImpl ticketVerificationService;

    @Captor
    private ArgumentCaptor<Reservation> reservationCaptor;

    private Reservation validUnusedReservation;
    private Reservation usedReservation;
    private static final String VALID_KEY = "valid-ticket-key-123";
    private static final String USED_KEY = "used-ticket-key-456";
    private static final String INVALID_KEY = "invalid-key";

    @BeforeEach
    void setUp() {
        // Configuration d'une réservation valide non utilisée
        validUnusedReservation = new Reservation();
        validUnusedReservation.setReservationId(1L);
        validUnusedReservation.setFinalKey(VALID_KEY);
        validUnusedReservation.setIsUsed(false);

        // Utiliser java.util.Date au lieu de LocalDateTime
        validUnusedReservation.setReservationDate(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)); // -1 jour

        // Configuration d'une réservation déjà utilisée
        usedReservation = new Reservation();
        usedReservation.setReservationId(2L);
        usedReservation.setFinalKey(USED_KEY);
        usedReservation.setIsUsed(true);

        // Date de réservation il y a 2 jours
        usedReservation.setReservationDate(new Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000));

        // Date d'utilisation il y a 5 heures
        // Correction: utiliser setUsageDate et non setUsedDate
        usedReservation.setUsageDate(new Date(System.currentTimeMillis() - 5 * 60 * 60 * 1000));
    }


    // Tests pour verifyTicket

    @Test
    @DisplayName("verifyTicket - Devrait valider un billet valide et non utilisé")
    void verifyTicket_WithValidUnusedTicket_ShouldMarkAsUsedAndReturnReservation() {
        // Given
        when(reservationRepository.findByFinalKey(VALID_KEY)).thenReturn(Optional.of(validUnusedReservation));

        // When
        Optional<Reservation> result = ticketVerificationService.verifyTicket(VALID_KEY);

        // Then
        assertTrue(result.isPresent());
        assertEquals(validUnusedReservation.getReservationId(), result.get().getReservationId());

        // Vérification que le billet a été marqué comme utilisé
        verify(reservationRepository).save(reservationCaptor.capture());
        Reservation savedReservation = reservationCaptor.getValue();
        assertTrue(savedReservation.getIsUsed());
        assertNotNull(savedReservation.getUsageDate());
    }

    @Test
    @DisplayName("verifyTicket - Ne devrait pas valider un billet déjà utilisé")
    void verifyTicket_WithAlreadyUsedTicket_ShouldReturnEmpty() {
        // Given
        when(reservationRepository.findByFinalKey(USED_KEY)).thenReturn(Optional.of(usedReservation));

        // When
        Optional<Reservation> result = ticketVerificationService.verifyTicket(USED_KEY);

        // Then
        assertTrue(result.isEmpty());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("verifyTicket - Ne devrait pas valider une clé de billet invalide")
    void verifyTicket_WithInvalidKey_ShouldReturnEmpty() {
        // Given
        when(reservationRepository.findByFinalKey(INVALID_KEY)).thenReturn(Optional.empty());

        // When
        Optional<Reservation> result = ticketVerificationService.verifyTicket(INVALID_KEY);

        // Then
        assertTrue(result.isEmpty());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    // Tests pour checkTicketValidity

    @Test
    @DisplayName("checkTicketValidity - Devrait retourner un billet valide et non utilisé sans le modifier")
    void checkTicketValidity_WithValidUnusedTicket_ShouldReturnReservation() {
        // Given
        when(reservationRepository.findByFinalKey(VALID_KEY)).thenReturn(Optional.of(validUnusedReservation));

        // When
        Optional<Reservation> result = ticketVerificationService.checkTicketValidity(VALID_KEY);

        // Then
        assertTrue(result.isPresent());
        assertEquals(validUnusedReservation.getReservationId(), result.get().getReservationId());
        assertFalse(result.get().getIsUsed());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("checkTicketValidity - Ne devrait pas valider un billet déjà utilisé")
    void checkTicketValidity_WithAlreadyUsedTicket_ShouldReturnEmpty() {
        // Given
        when(reservationRepository.findByFinalKey(USED_KEY)).thenReturn(Optional.of(usedReservation));

        // When
        Optional<Reservation> result = ticketVerificationService.checkTicketValidity(USED_KEY);

        // Then
        assertTrue(result.isEmpty());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("checkTicketValidity - Ne devrait pas valider une clé de billet invalide")
    void checkTicketValidity_WithInvalidKey_ShouldReturnEmpty() {
        // Given
        when(reservationRepository.findByFinalKey(INVALID_KEY)).thenReturn(Optional.empty());

        // When
        Optional<Reservation> result = ticketVerificationService.checkTicketValidity(INVALID_KEY);

        // Then
        assertTrue(result.isEmpty());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("verifyTicket - Devrait enregistrer la date d'utilisation")
    void verifyTicket_ShouldSaveUsedDate() {
        // Given
        Date beforeTest = new Date();
        when(reservationRepository.findByFinalKey(VALID_KEY)).thenReturn(Optional.of(validUnusedReservation));

        // When
        ticketVerificationService.verifyTicket(VALID_KEY);

        // Then
        verify(reservationRepository).save(reservationCaptor.capture());
        Reservation savedReservation = reservationCaptor.getValue();

        // Vérifier que la date d'utilisation est définie
        Date usageDate = savedReservation.getUsageDate(); // Utiliser getUsageDate()
        assertNotNull(usageDate);

        // La date d'utilisation doit être postérieure ou égale au début du test
        assertTrue(usageDate.compareTo(beforeTest) >= 0);
        // Et antérieure ou égale à maintenant
        assertTrue(usageDate.compareTo(new Date()) <= 0);
    }
}
