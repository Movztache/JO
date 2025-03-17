package com.example.jeuxolympiques.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;
import jakarta.validation.constraints.*;

@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @NotNull()
    @Temporal(TemporalType.TIMESTAMP)
    private Date reservationDate;

    @NotBlank()
    private String reservationKey;

    @NotBlank()
    private String qrCode;

    private String finalKey;

    @NotNull()
    @Min(value = 1, message = "La quantité doit être d'au moins 1")
    @Max(value = 10000, message = "La quantité ne peut pas dépasser 10000")
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "user_app_id", nullable = false)
    @NotNull()
    private UserApp userApp;

    @ManyToOne
    @JoinColumn(name = "offer_id", nullable = false)
    @NotNull()
    private Offer offer;


    @PrePersist
    private void generateQrCode() {
        this.qrCode = UUID.randomUUID().toString();
    }

    public Long getReservationId() {
        return reservationId;
    }
    public void setReservationId(Long id) {
        this.reservationId = id;
    }
    public Date getReservationDate() {
        return reservationDate;
    }
    public void setReservationDate(Date reservationDate) {
        this.reservationDate = reservationDate;
    }
    public String getReservationKey() {
        return reservationKey;
    }
    public void setReservationKey(String reservationKey) {
        this.reservationKey = reservationKey;
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
    public UserApp getUserApp() {
        return userApp;
    }
    public void setUserApp(UserApp userApp) {
        this.userApp = userApp;
    }
    public Offer getOffer() {
        return offer;
    }
    public void setOffer(Offer offer) {
        this.offer = offer;
    }
    public void setFinalKey() {
        this.finalKey = this.reservationKey + this.quantity;
    }
    public void setReservationKey() {
        this.reservationKey = this.qrCode.substring(0, 8);
    }
    public void setQuantity() {
        this.quantity = Integer.parseInt(this.qrCode.substring(8));
    }
    public void setReservationDate() {
        this.reservationDate = new Date();
    }

}
