package com.example.vibetickets.service.impl;

import com.example.vibetickets.repository.PaymentRepository;
import com.example.vibetickets.service.SalesStatisticsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SalesStatisticsServiceImpl implements SalesStatisticsService {

    private final PaymentRepository paymentRepository;

    public SalesStatisticsServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }
    /**
     * Récupère le nombre de ventes par offre
     */
    @Override
    public Map<Long, Integer> getSalesByOffer() {
        List<Object[]> results = paymentRepository.countAndSumByOffer();
        Map<Long, Integer> salesByOffer = new HashMap<>();

        for (Object[] result : results) {
            Long offerId = (Long) result[0];
            Long count = (Long) result[1];
            salesByOffer.put(offerId, count.intValue());
        }

        return salesByOffer;
    }

    /**
     * Récupère le chiffre d'affaires par offre
     */
    @Override
    public Map<Long, BigDecimal> getRevenusByOffer() {
        List<Object[]> results = paymentRepository.countAndSumByOffer();
        Map<Long, BigDecimal> revenueByOffer = new HashMap<>();

        for (Object[] result : results) {
            Long offerId = (Long) result[0];
            BigDecimal revenue = (BigDecimal) result[2];
            revenueByOffer.put(offerId, revenue);
        }

        return revenueByOffer;
    }

    @Override
    public Map<Long, Object> getSalesStatisticsByPeriod(LocalDateTime start, LocalDateTime end) {
        List<Object[]> results = paymentRepository.findSalesStatisticsByPeriod(start, end);
        Map<Long, Object> statisticsByOffer = new HashMap<>();

        for (Object[] result : results) {
            Long offerId = (Long) result[0];
            Long count = (Long) result[1];
            BigDecimal totalRevenue = (BigDecimal) result[2];
            BigDecimal averageAmount = (BigDecimal) result[3];

            Map<String, Object> stats = new HashMap<>();
            stats.put("salesCount", count);
            stats.put("totalRevenue", totalRevenue);
            stats.put("averageTicketPrice", averageAmount);

            statisticsByOffer.put(offerId, stats);
        }

        return statisticsByOffer;
    }



}