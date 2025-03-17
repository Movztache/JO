package com.example.jeuxolympiques.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "offer")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long offerId;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String name;

    @Size(max = 1000, message = "La description ne doit pas dépasser 1000 caractères")
    private String description;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être supérieur à 0")
    private Double price;

    @NotBlank(message = "Le type d'offre est obligatoire")
    @Size(max = 50, message = "Le type d'offre ne doit pas dépasser 50 caractères")
    private String offerType;

    @Min(value = 1)
    @Column(name = "person_count", nullable = false)
    private Integer personCount;

    @Column(nullable = false)
    private boolean available = true;

    public void setOfferId(Long id) {
        this.offerId = id;
    }
    public Long getOfferId() {
        return offerId;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getOfferType() {
        return offerType;
    }

    public void setOfferType(String offerType) {
        this.offerType = offerType;
    }

    public boolean getIsAvailable() {
        return available;
    }
    public void setIsAvailable(boolean available) {
        this.available = available;
    }
    public Integer getPersonCount() {
        return personCount;
    }
    public void setPersonCount(Integer personCount) {
        this.personCount = personCount;
    }

    public Offer() {

    }
    public Offer(String name, String description, Double price, String offerType, Integer personCount) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.offerType = offerType;
        this.personCount = personCount;
    }

}
