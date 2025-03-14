package com.example.jeuxolympiques.model;

import jakarta.persistence.*;

@Entity
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "user_app_id",nullable = false)
    private UserApp userApp;

    @ManyToOne
    @JoinColumn(name = "offer_id",nullable = false)
    private Offer offer;
}
