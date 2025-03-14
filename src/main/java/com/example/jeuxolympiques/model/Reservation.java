package com.example.jeuxolympiques.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date reservationDate;
    private String reservationKey;
    private String qrCode;
    private String finalKey;
    private Integer quantity;


    @ManyToOne
    @JoinColumn(name = "user_app_id",nullable = false)
    private UserApp userApp;

    @ManyToOne
    @JoinColumn(name = "offer_id",nullable = false)
    private Offer offer;

    @PrePersist
    private void generateQrCode() {
        this.qrCode = UUID.randomUUID().toString();
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
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
