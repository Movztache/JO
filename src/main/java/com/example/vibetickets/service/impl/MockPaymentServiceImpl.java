package com.example.vibetickets.service.impl;

import com.example.vibetickets.model.Payment;
import com.example.vibetickets.model.Reservation;
import com.example.vibetickets.repository.PaymentRepository;
import com.example.vibetickets.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
public class MockPaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(MockPaymentServiceImpl.class);
    private final PaymentRepository paymentRepository;
    private final Random random = new Random();

    public MockPaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Traite un paiement simulé pour une réservation.
     * Effectue des validations basiques sur les données de carte
     * et simule un taux de réussite de 95%.
     */
    @Override
    @Transactional
    public boolean processPayment(BigDecimal amount, String cardNumber, String expiryDate, String cvv, Reservation reservation) {
        // Validation basique du numéro de carte
        if (cardNumber == null || cardNumber.length() < 13 || cardNumber.length() > 19) {
            logger.warn("Paiement refusé: numéro de carte invalide");
            saveFailedTransaction(amount, cardNumber, reservation);
            return false;
        }

        // Validation basique de la date d'expiration (format MM/YY)
        if (expiryDate == null || !expiryDate.matches("^(0[1-9]|1[0-2])/[0-9]{2}$")) {
            logger.warn("Paiement refusé: date d'expiration invalide");
            saveFailedTransaction(amount, cardNumber, reservation);
            return false;
        }

        // Validation basique du CVV (3 ou 4 chiffres)
        if (cvv == null || !cvv.matches("^[0-9]{3,4}$")) {
            logger.warn("Paiement refusé: CVV invalide");
            saveFailedTransaction(amount, cardNumber, reservation);
            return false;
        }

        // Simulation d'un taux de réussite de 95%
        boolean paymentSuccessful = random.nextInt(100) < 95;

        if (paymentSuccessful) {
            // Générer un ID unique pour la transaction réussie
            String transactionId = generateTransactionId();
            saveSuccessfulTransaction(transactionId, amount, cardNumber, reservation);
            logger.info("Paiement accepté - Montant: {} € - Transaction ID: {}", amount, transactionId);
            return true;
        } else {
            // Enregistrer l'échec du paiement
            logger.warn("Paiement refusé - Montant: {} €", amount);
            saveFailedTransaction(amount, cardNumber, reservation);
            return false;
        }
    }

    /**
     * Enregistre un paiement réussi dans la base de données
     */
    private void saveSuccessfulTransaction(String transactionId, BigDecimal amount, String cardNumber, Reservation reservation) {
        Payment payment = new Payment();
        payment.setTransactionId(transactionId);
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus("COMPLETED");
        payment.setReservation(reservation);
        payment.setPaymentMethod("CREDIT_CARD");

        if (cardNumber != null && cardNumber.length() >= 4) {
            payment.setCardLastDigits(cardNumber.substring(cardNumber.length() - 4));
        }

        paymentRepository.save(payment);
    }

    /**
     * Enregistre un paiement échoué dans la base de données
     */
    private void saveFailedTransaction(BigDecimal amount, String cardNumber, Reservation reservation) {
        Payment payment = new Payment();
        payment.setTransactionId(generateTransactionId());
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus("FAILED");
        payment.setReservation(reservation);
        payment.setPaymentMethod("CREDIT_CARD");

        // Ne stocker que les 4 derniers chiffres pour la sécurité
        if (cardNumber != null && cardNumber.length() >= 4) {
            payment.setCardLastDigits(cardNumber.substring(cardNumber.length() - 4));
        }

        paymentRepository.save(payment);
    }

    /**
     * Annule un paiement en changeant son statut en "REFUNDED"
     */
    @Override
    @Transactional
    public boolean cancelPayment(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElse(null);

        // Vérifier si la transaction existe et peut être annulée
        if (payment != null && "COMPLETED".equals(payment.getStatus())) {
            payment.setStatus("REFUNDED");
            paymentRepository.save(payment);

            logger.info("Paiement annulé - Montant: {} € - Transaction ID: {}",
                    payment.getAmount(), transactionId);
            return true;
        }

        logger.warn("Annulation impossible - Transaction ID inconnu ou déjà annulée: {}", transactionId);
        return false;
    }

    /**
     * Génère un identifiant unique pour une transaction
     */
    @Override
    public String generateTransactionId() {
        return UUID.randomUUID().toString();
    }
}