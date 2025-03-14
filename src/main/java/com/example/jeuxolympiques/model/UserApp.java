package com.example.jeuxolympiques.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_app")
public class UserApp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50, message = "Le nom ne doit pas dépasser 50 caractères")
    private String lastName;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 50, message = "Le prénom ne doit pas dépasser 50 caractères")
    private String firstName;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Column(unique = true) // Garantit l'unicité de l'email
    private String email;

    @Column(name = "user_key")
    private String userKey;

    @ManyToOne
    @JoinColumn(name = "rule_id", nullable = true)
    private Rule rule;

    @OneToMany(mappedBy = "userApp", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cart> carts = new ArrayList<>();

    @OneToMany(mappedBy = "userApp", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Log> logs = new ArrayList<>();

    @OneToMany(mappedBy = "userApp", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();

    // Constructeurs
    public UserApp() {

    }

    public UserApp(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public Rule getRule() {
        return rule;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getUserKey() {
        return userKey;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public List<Cart> getCarts() {
        return carts;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public List<Log> getLogs() {
        return logs;
    }

    // Méthodes utilitaires pour gérer les relations
    public void addCart(Cart cart) {
        carts.add(cart);
        cart.setUserApp(this);
    }

    public void removeCart(Cart cart) {
        carts.remove(cart);
        cart.setUserApp(null);
    }

    public void addLog(Log log) {
        logs.add(log);
        log.setUserApp(this);
    }

    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
        reservation.setUserApp(this);
    }

    public void removeReservation(Reservation reservation) {
        reservations.remove(reservation);
        reservation.setUserApp(null);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        // Vérifications que l'objet n'est pas null et est de la même classe
        if (obj == null || getClass() != obj.getClass()) return false;
        UserApp userApp = (UserApp) obj;
        return id != null && id.equals(userApp.getId());
    }

    @Override
    public int hashCode() {
        // Si l'id n'est pas null, on utilise son hashCode, sinon on retourne 0.
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "MyUser {" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", rule=" + (rule != null ? rule.getClass().getSimpleName() + "@" + Integer.toHexString(rule.hashCode()) : "null") + "}";
    }
}