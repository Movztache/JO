package com.example.vibetickets.controller;

import com.example.vibetickets.service.SalesStatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class SalesStatisticsController {

    private final SalesStatisticsService salesStatisticsService;

    public SalesStatisticsController(SalesStatisticsService salesStatisticsService) {
        this.salesStatisticsService = salesStatisticsService;
    }

    /**
     * Récupère le nombre de ventes par offre
     * @return Map contenant l'ID de l'offre et le nombre de ventes
     */
    @GetMapping("/sales-by-offer")
    public ResponseEntity<Map<Long, Integer>> getSalesByOffer() {
        Map<Long, Integer> salesByOffer = salesStatisticsService.getSalesByOffer();
        return ResponseEntity.ok(salesByOffer);
    }

    /**
     * Récupère le chiffre d'affaires par offre
     * @return Map contenant l'ID de l'offre et le chiffre d'affaires
     */
    @GetMapping("/revenues-by-offer")
    public ResponseEntity<Map<Long, BigDecimal>> getRevenusByOffer() {
        Map<Long, BigDecimal> revenuesByOffer = salesStatisticsService.getRevenusByOffer();
        return ResponseEntity.ok(revenuesByOffer);
    }

    /**
     * Récupère les statistiques de vente par période
     * @param start Date de début de la période
     * @param end Date de fin de la période
     * @return Map contenant les statistiques de vente par offre pour la période
     */
    @GetMapping("/by-period")
    public ResponseEntity<Map<Long, Object>> getSalesStatisticsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        Map<Long, Object> statistics = salesStatisticsService.getSalesStatisticsByPeriod(start, end);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Obtient un résumé global des statistiques de vente
     * @return Un résumé des statistiques de vente
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getStatisticsSummary() {
        Map<Long, Integer> sales = salesStatisticsService.getSalesByOffer();
        Map<Long, BigDecimal> revenues = salesStatisticsService.getRevenusByOffer();

        int totalSales = sales.values().stream().mapToInt(Integer::intValue).sum();
        BigDecimal totalRevenue = revenues.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalSales", totalSales);
        summary.put("totalRevenue", totalRevenue);
        summary.put("salesByOffer", sales);
        summary.put("revenueByOffer", revenues);

        return ResponseEntity.ok(summary);
    }
}