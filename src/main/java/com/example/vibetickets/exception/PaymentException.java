package com.example.vibetickets.exception;


public class PaymentException extends RuntimeException {
    /**
     * Crée une nouvelle exception avec le message spécifié
     */
    public PaymentException(String message) {
        super(message);
    }

    /**
     * Crée une nouvelle exception avec le message et la cause spécifiés
     */
    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}