package com.example.jeuxolympiques.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_app")
public class UserApp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String lastName;
    private String firstName;
    private String password;
    private String email;
    private String userKey;

    @ManyToOne
    @JoinColumn(name = "rule_id",nullable = true)
    private Rule rule;

    @OneToMany(mappedBy = "userApp", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Cart> carts = new ArrayList<>();

    @OneToMany(mappedBy = "userApp", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Log> logs = new ArrayList<>();

    @OneToMany(mappedBy = "userApp", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
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

}
