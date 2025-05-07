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

    @Column(unique = true)
    private String finalKey;

    private boolean isUsed;

    private Date usageDate;

    @NotNull()
    @Column(nullable = false)
    private String status = "PENDING"; // Valeur par défaut

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


    public void generateSecureQrCode() {
        if (this.userApp != null && this.reservationKey != null && this.quantity != null) {
            // Format: reservationKey + userKey + quantity
            this.qrCode = this.reservationKey + "|" + this.userApp.getUserKey() + "|" + this.quantity;
        }
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
    public void setFinalKey() {
        if (this.userApp != null) {
            this.finalKey = this.reservationKey + this.userApp.getUserKey() + this.quantity;
        } else {
            this.finalKey = this.reservationKey + this.quantity;
        }
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

    // Méthode pour extraire la reservationKey du QRCode
    public void setReservationKey() {
        if (this.qrCode != null && this.qrCode.contains("|")) {
            String[] parts = this.qrCode.split("\\|");
            if (parts.length >= 1) {
                this.reservationKey = parts[0];
            }
        }
    }

    // Méthode pour extraire la quantité du QRCode
    public void setQuantity() {
        if (this.qrCode != null && this.qrCode.contains("|")) {
            String[] parts = this.qrCode.split("\\|");
            if (parts.length >= 3) {
                try {
                    this.quantity = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    this.quantity = 1; // Valeur par défaut
                }
            }
        }
    }
    public void setReservationDate() {
        this.reservationDate = new Date();
    }

    public boolean getIsUsed() {
        return isUsed;
    }
    public void setIsUsed(boolean used) {
        isUsed = used;
    }
    public Date getUsageDate() {
        return usageDate;
    }
    public void setUsageDate(Date usageDate) {
        this.usageDate = usageDate;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

}
