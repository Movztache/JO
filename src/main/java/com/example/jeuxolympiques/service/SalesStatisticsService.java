package com.example.jeuxolympiques.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public interface SalesStatisticsService {
    /**
     * Récupère le nombre de ventes par offre
     * @return Map avec l'id de l'offre comme clé et le nombre de ventes comme valeur
     */
    Map<Long, Integer> getSalesByOffer();

    /**
     * Récupère le chiffre d'affaires par offre
     * @return Map avec l'id de l'offre comme clé et le montant des ventes comme valeur
     */
    Map<Long, BigDecimal> getRevenusByOffer();

    /**
     * Récupère les statistiques de vente sur une période donnée
     * @param start Date de début
     * @param end Date de fin
     * @return Map avec l'id de l'offre comme clé et un objet contenant les statistiques comme valeur
     */
    Map<Long, Object> getSalesStatisticsByPeriod(LocalDateTime start, LocalDateTime end);

}