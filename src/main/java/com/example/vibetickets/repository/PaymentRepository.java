package com.example.vibetickets.repository;

import com.example.vibetickets.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Recherche un paiement par son ID de transaction
     * @param transactionId L'ID de la transaction
     * @return Le paiement correspondant, s'il existe
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Compte les ventes et somme les revenus par offre
     * @return Une liste d'objets contenant [offerId, count, sum]
     */
    @Query("SELECT r.offer.offerId, COUNT(p), SUM(p.amount) FROM Payment p JOIN p.reservation r GROUP BY r.offer.offerId")
    List<Object[]> countAndSumByOffer();

    /**
     * Trouve les statistiques de vente par période
     * @param start Date de début
     * @param end Date de fin
     * @return Une liste d'objets contenant [offerId, count, totalRevenue, averageAmount]
     */
    @Query("SELECT r.offer.offerId, COUNT(p), SUM(p.amount), AVG(p.amount) " +
            "FROM Payment p JOIN p.reservation r " +
            "WHERE p.paymentDate BETWEEN :start AND :end " +
            "GROUP BY r.offer.offerId")
    List<Object[]> findSalesStatisticsByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}