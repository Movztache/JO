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
}
