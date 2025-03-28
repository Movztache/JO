package com.example.jeuxolympiques.repository;

import com.example.jeuxolympiques.model.Payment;
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
     * Recherche un paiement par son identifiant de transaction
     */
    Optional<Payment> findByTransactionId(String transactionId);


    /**
     * Compte le nombre de paiements et calcule le montant total par offre
     * Utilisé pour les statistiques de vente
     */
    @Query("SELECT p.reservation.offer.offerId, COUNT(p), SUM(p.amount) FROM Payment p " +
            "WHERE p.status = 'COMPLETED' GROUP BY p.reservation.offer.offerId")
    List<Object[]> countAndSumByOffer();


    @Query("SELECT p.reservation.offer.offerId, COUNT(p), SUM(p.amount), AVG(p.amount) " +
            "FROM Payment p " +
            "WHERE p.paymentDate BETWEEN :start AND :end AND p.status = 'COMPLETED' " +
            "GROUP BY p.reservation.offer.offerId")
    List<Object[]> findSalesStatisticsByPeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);


}