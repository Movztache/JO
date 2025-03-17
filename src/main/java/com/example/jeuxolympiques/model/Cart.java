package com.example.jeuxolympiques.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être d'au moins 1")
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "user_app_id", nullable = false)
    @NotNull()
    private UserApp userApp;

    @ManyToOne
    @JoinColumn(name = "offer_id", nullable = false)
    @NotNull()
    private Offer offer;


    public Long getCartId() {
        return cartId;
    }
    public void setCartId(Long id) {
        this.cartId = id;
    }
    public Integer getQuantity() {
        return quantity;
    }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    public UserApp getUserApp() {
    return userApp;}

    public void setUserApp(UserApp userApp) {
        this.userApp = userApp;
    }
}
