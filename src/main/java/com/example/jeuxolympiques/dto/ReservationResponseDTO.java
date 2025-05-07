package com.example.jeuxolympiques.dto;

import com.example.jeuxolympiques.model.Reservation;
import java.math.BigDecimal;
import java.util.Date;

/**
 * DTO pour la réponse après création d'une réservation
 * Contient uniquement les informations nécessaires pour le client
 */
public class ReservationResponseDTO {

    private Long reservationId;
    private Date reservationDate;
    private String qrCode;
    private String finalKey;
    private Integer quantity;
    private Long offerId;
    private String offerName;
    private BigDecimal offerPrice;
    private BigDecimal totalPrice;

    // Constructeur par défaut
    public ReservationResponseDTO() {
    }

    // Constructeur à partir d'une entité Reservation
    public ReservationResponseDTO(Reservation reservation) {
        this.reservationId = reservation.getReservationId();
        this.reservationDate = reservation.getReservationDate();
        this.qrCode = reservation.getQrCode();
        this.finalKey = reservation.getFinalKey();
        this.quantity = reservation.getQuantity();

        if (reservation.getOffer() != null) {
            this.offerId = reservation.getOffer().getOfferId();
            this.offerName = reservation.getOffer().getName();
            this.offerPrice = reservation.getOffer().getPrice();
            this.totalPrice = reservation.getOffer().getPrice().multiply(BigDecimal.valueOf(reservation.getQuantity()));
        }

        // S'assurer que la clé finale est bien définie
        if (this.finalKey == null || this.finalKey.isEmpty()) {
            throw new IllegalStateException("La clé finale n'a pas été générée correctement");
        }
    }

    // Getters et setters
    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Date getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(Date reservationDate) {
        this.reservationDate = reservationDate;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getFinalKey() {
        return finalKey;
    }

    public void setFinalKey(String finalKey) {
        this.finalKey = finalKey;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getOfferId() {
        return offerId;
    }

    public void setOfferId(Long offerId) {
        this.offerId = offerId;
    }

    public String getOfferName() {
        return offerName;
    }

    public void setOfferName(String offerName) {
        this.offerName = offerName;
    }

    public BigDecimal getOfferPrice() {
        return offerPrice;
    }

    public void setOfferPrice(BigDecimal offerPrice) {
        this.offerPrice = offerPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
