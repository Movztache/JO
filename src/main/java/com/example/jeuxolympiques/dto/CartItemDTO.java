package com.example.jeuxolympiques.dto;

import java.math.BigDecimal;

public class CartItemDTO {

    private Long cartId;
    private Long offerId;
    private String offerName;
    private String offerDescription;
    private BigDecimal offerPrice;
    private Integer quantity;
    private BigDecimal totalPrice; // Prix de l'offre × quantité


    public CartItemDTO() {
    }

    public CartItemDTO(Long cartId, Long offerId, String offerName,
                       String offerDescription, BigDecimal offerPrice,
                       Integer quantity) {
        this.cartId = cartId;
        this.offerId = offerId;
        this.offerName = offerName;
        this.offerDescription = offerDescription;
        this.offerPrice = offerPrice;
        this.quantity = quantity;
        this.totalPrice = offerPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
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

    public String getOfferDescription() {
        return offerDescription;
    }

    public void setOfferDescription(String offerDescription) {
        this.offerDescription = offerDescription;
    }

    public BigDecimal getOfferPrice() {
        return offerPrice;
    }

    public void setOfferPrice(BigDecimal offerPrice) {
        this.offerPrice = offerPrice;
        // Recalculer le prix total si la quantité est définie
        if (this.quantity != null) {
            this.totalPrice = offerPrice.multiply(BigDecimal.valueOf(this.quantity));
        }
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        // Recalculer le prix total si le prix est défini
        if (this.offerPrice != null) {
            this.totalPrice = this.offerPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}