package com.example.jeuxolympiques;

import com.example.jeuxolympiques.repository.PaymentRepository;
import com.example.jeuxolympiques.service.impl.SalesStatisticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesStatisticsServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private SalesStatisticsServiceImpl salesStatisticsService;

    private List<Object[]> mockCountAndSumResults;
    private List<Object[]> mockPeriodResults;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @BeforeEach
    void setUp() {
        // Préparation des données de test pour countAndSumByOffer
        mockCountAndSumResults = Arrays.asList(
                new Object[]{1L, 5L, new BigDecimal("500.00")},
                new Object[]{2L, 3L, new BigDecimal("300.00")},
                new Object[]{3L, 0L, new BigDecimal("0.00")}
        );

        // Préparation des données de test pour findSalesStatisticsByPeriod
        mockPeriodResults = Arrays.asList(
                new Object[]{1L, 2L, new BigDecimal("200.00"), new BigDecimal("100.00")},
                new Object[]{2L, 1L, new BigDecimal("150.00"), new BigDecimal("150.00")}
        );

        // Définition de la période de test
        startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        endDate = LocalDateTime.of(2024, 1, 31, 23, 59);
    }

    @Test
    @DisplayName("Test getSalesByOffer - Devrait retourner le nombre de ventes par offre")
    void testGetSalesByOffer() {
        // Given
        when(paymentRepository.countAndSumByOffer()).thenReturn(mockCountAndSumResults);

        // When
        Map<Long, Integer> result = salesStatisticsService.getSalesByOffer();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(5, result.get(1L));
        assertEquals(3, result.get(2L));
        assertEquals(0, result.get(3L));
        verify(paymentRepository, times(1)).countAndSumByOffer();
    }

    @Test
    @DisplayName("Test getRevenusByOffer - Devrait retourner le chiffre d'affaires par offre")
    void testGetRevenusByOffer() {
        // Given
        when(paymentRepository.countAndSumByOffer()).thenReturn(mockCountAndSumResults);

        // When
        Map<Long, BigDecimal> result = salesStatisticsService.getRevenusByOffer();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(new BigDecimal("500.00"), result.get(1L));
        assertEquals(new BigDecimal("300.00"), result.get(2L));
        assertEquals(new BigDecimal("0.00"), result.get(3L));
        verify(paymentRepository, times(1)).countAndSumByOffer();
    }

    @Test
    @DisplayName("Test getSalesStatisticsByPeriod - Devrait retourner les statistiques de vente pour la période spécifiée")
    void testGetSalesStatisticsByPeriod() {
        // Given
        when(paymentRepository.findSalesStatisticsByPeriod(startDate, endDate)).thenReturn(mockPeriodResults);

        // When
        Map<Long, Object> result = salesStatisticsService.getSalesStatisticsByPeriod(startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        // Vérification des statistiques pour l'offre 1
        @SuppressWarnings("unchecked")
        Map<String, Object> stats1 = (Map<String, Object>) result.get(1L);
        assertEquals(2L, stats1.get("salesCount"));
        assertEquals(new BigDecimal("200.00"), stats1.get("totalRevenue"));
        assertEquals(new BigDecimal("100.00"), stats1.get("averageTicketPrice"));

        // Vérification des statistiques pour l'offre 2
        @SuppressWarnings("unchecked")
        Map<String, Object> stats2 = (Map<String, Object>) result.get(2L);
        assertEquals(1L, stats2.get("salesCount"));
        assertEquals(new BigDecimal("150.00"), stats2.get("totalRevenue"));
        assertEquals(new BigDecimal("150.00"), stats2.get("averageTicketPrice"));

        verify(paymentRepository, times(1)).findSalesStatisticsByPeriod(startDate, endDate);
    }

    @Test
    @DisplayName("Test getSalesStatisticsByPeriod - Devrait retourner une Map vide quand aucun résultat n'est trouvé")
    void testGetSalesStatisticsByPeriodWithNoResults() {
        // Given
        when(paymentRepository.findSalesStatisticsByPeriod(startDate, endDate)).thenReturn(List.of());

        // When
        Map<Long, Object> result = salesStatisticsService.getSalesStatisticsByPeriod(startDate, endDate);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(paymentRepository, times(1)).findSalesStatisticsByPeriod(startDate, endDate);
    }
}