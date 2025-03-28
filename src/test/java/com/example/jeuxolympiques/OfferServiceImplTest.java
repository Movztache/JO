package com.example.jeuxolympiques;

import com.example.jeuxolympiques.model.Offer;
import com.example.jeuxolympiques.repository.OfferRepository;
import com.example.jeuxolympiques.service.impl.OfferServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OfferServiceImplTest {

    @Mock
    private OfferRepository offerRepository;

    @InjectMocks
    private OfferServiceImpl offerService;

    private Offer offer1;
    private Offer offer2;

    @BeforeEach
    public void setup() {
        // Création de données de test
        offer1 = new Offer("Offre d'essai 1", "Description de test 1", 29.99, "TICKET", 2);
        offer1.setOfferId(1L);
        offer1.setIsAvailable(true);

        offer2 = new Offer("Offre d'essai 2", "Description de test 2", 49.99, "ACCOMMODATION", 4);
        offer2.setOfferId(2L);
        offer2.setIsAvailable(false);
    }

    @Test
    public void testGetAllOffers() {
        // Given
        when(offerRepository.findAll()).thenReturn(Arrays.asList(offer1, offer2));

        // When
        List<Offer> offers = offerService.getAllOffers();

        // Then
        assertThat(offers).hasSize(2);
        assertThat(offers).contains(offer1, offer2);
        verify(offerRepository, times(1)).findAll();
    }

    @Test
    public void testGetOfferById_ExistingOffer() {
        // Given
        when(offerRepository.findByOfferId(1L)).thenReturn(offer1);

        // When
        Offer foundOffer = offerService.getOfferById(1L);

        // Then
        assertThat(foundOffer).isNotNull();
        assertThat(foundOffer.getOfferId()).isEqualTo(1L);
        assertThat(foundOffer.getName()).isEqualTo("Offre d'essai 1");
        verify(offerRepository, times(1)).findByOfferId(1L);
    }

    @Test
    public void testGetOfferById_NonExistingOffer() {
        // Given
        when(offerRepository.findByOfferId(99L)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> offerService.getOfferById(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Offre non trouvée");

        verify(offerRepository, times(1)).findByOfferId(99L);
    }

    @Test
    public void testGetOffersByType() {
        // Given
        when(offerRepository.findByOfferType("TICKET")).thenReturn(List.of(offer1));

        // When
        List<Offer> offers = offerService.getOffersByType("TICKET");

        // Then
        assertThat(offers).hasSize(1);
        assertThat(offers.get(0).getOfferType()).isEqualTo("TICKET");
        verify(offerRepository, times(1)).findByOfferType("TICKET");
    }

    @Test
    public void testGetOffersByType_EmptyType() {
        // When & Then
        assertThatThrownBy(() -> offerService.getOffersByType(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("type d'offre ne peut pas être vide");

        verify(offerRepository, never()).findByOfferType(any());
    }

    @Test
    public void testSaveOffer() {
        // Given
        Offer newOffer = new Offer("Nouvelle offre", "Description nouvelle", 39.99, "TRANSPORT", 3);
        when(offerRepository.save(any(Offer.class))).thenReturn(newOffer);

        // When
        Offer savedOffer = offerService.saveOffer(newOffer);

        // Then
        assertThat(savedOffer).isNotNull();
        assertThat(savedOffer.getName()).isEqualTo("Nouvelle offre");
        verify(offerRepository, times(1)).save(newOffer);
    }

    @Test
    public void testSaveOffer_ValidationsFailure() {
        // Given
        Offer invalidOffer = new Offer();
        invalidOffer.setName("");  // Invalid: nom vide.

        // When & Then
        assertThatThrownBy(() -> offerService.saveOffer(invalidOffer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nom de l'offre ne peut pas être vide");

        verify(offerRepository, never()).save(any());
    }

    @Test
    public void testDeleteOffer_ExistingOffer() {
        // Given
        when(offerRepository.findByOfferId(1L)).thenReturn(offer1);

        // When
        offerService.deleteOffer(1L);

        // Then
        verify(offerRepository, times(1)).findByOfferId(1L);
        verify(offerRepository, times(1)).delete(offer1);
    }

    @Test
    public void testDeleteOffer_NonExistingOffer() {
        // Given
        when(offerRepository.findByOfferId(99L)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> offerService.deleteOffer(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Offre non trouvée");

        verify(offerRepository, times(1)).findByOfferId(99L);
        verify(offerRepository, never()).delete(any());
    }

    @Test
    public void testExistsById_Existing() {
        // Given
        when(offerRepository.findByOfferId(1L)).thenReturn(offer1);

        // When
        boolean exists = offerService.existsById(1L);

        // Then
        assertThat(exists).isTrue();
        verify(offerRepository, times(1)).findByOfferId(1L);
    }

    @Test
    public void testExistsById_NonExisting() {
        // Given
        when(offerRepository.findByOfferId(99L)).thenReturn(null);

        // When
        boolean exists = offerService.existsById(99L);

        // Then
        assertThat(exists).isFalse();
        verify(offerRepository, times(1)).findByOfferId(99L);
    }

    @Test
    public void testGetAvailableOffers() {
        // Given
        List<Offer> allOffers = Arrays.asList(offer1, offer2);
        when(offerRepository.findAll()).thenReturn(allOffers);

        // When
        List<Offer> availableOffers = offerService.getAvailableOffers();

        // Then
        assertThat(availableOffers).hasSize(1);
        assertThat(availableOffers.get(0).getOfferId()).isEqualTo(1L);
        assertThat(availableOffers.get(0).getIsAvailable()).isTrue();
        verify(offerRepository, times(1)).findAll();
    }

    @Test
    public void testUpdateOffer() {
        // Given
        Offer updatedDetails = new Offer("Offre modifiée", "Nouvelle description", 59.99, "TICKET", 2);
        when(offerRepository.findByOfferId(1L)).thenReturn(offer1);
        when(offerRepository.save(any(Offer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Offer updatedOffer = offerService.updateOffer(1L, updatedDetails);

        // Then
        assertThat(updatedOffer).isNotNull();
        assertThat(updatedOffer.getOfferId()).isEqualTo(1L);
        assertThat(updatedOffer.getName()).isEqualTo("Offre modifiée");
        assertThat(updatedOffer.getDescription()).isEqualTo("Nouvelle description");
        assertThat(updatedOffer.getPrice()).isEqualTo(59.99);
        verify(offerRepository, times(1)).findByOfferId(1L);
        verify(offerRepository, times(1)).save(offer1);
    }

    @Test
    public void testUpdateOfferAvailability() {
        // Given
        when(offerRepository.findByOfferId(1L)).thenReturn(offer1);
        when(offerRepository.save(any(Offer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Offer updatedOffer = offerService.updateOfferAvailability(1L, false);

        // Then
        assertThat(updatedOffer).isNotNull();
        assertThat(updatedOffer.getIsAvailable()).isFalse();
        verify(offerRepository, times(1)).findByOfferId(1L);
        verify(offerRepository, times(1)).save(offer1);
    }
}