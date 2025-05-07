package com.example.jeuxolympiques.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO pour la création d'une nouvelle réservation
 */
public class ReservationCreateDTO {

    @NotNull(message = "L'ID de l'offre est obligatoire")
    private Long offerId;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être d'au moins 1")
    @Max(value = 10000, message = "La quantité ne peut pas dépasser 10000")
    private Integer quantity;

    @NotBlank(message = "Les informations de paiement sont obligatoires")
    @Pattern(regexp = "^[0-9]{16}\\|[0-9]{2}\\/[0-9]{2}\\|[0-9]{3,4}$",
             message = "Format des informations de paiement invalide. Format attendu: 'cardNumber|MM/YY|CVV'")
    private String paymentInfo;

    // Constructeur par défaut
    public ReservationCreateDTO() {
    }

    // Constructeur avec paramètres
    public ReservationCreateDTO(Long offerId, Integer quantity, String paymentInfo) {
        this.offerId = offerId;
        this.quantity = quantity;
        this.paymentInfo = paymentInfo;
    }

    // Getters et setters
    public Long getOfferId() {
        return offerId;
    }

    public void setOfferId(Long offerId) {
        this.offerId = offerId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(String paymentInfo) {
        this.paymentInfo = paymentInfo;
    }
}
