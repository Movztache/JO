package com.example.vibetickets.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;

    @NotBlank(message = "Le nom du rôle est obligatoire")
    private String name;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserApp> userApps = new ArrayList<>();

    // Constructeurs
    public Role() {}

    public Role(String name) {
        this.name = name;
    }

    // Getters et Setters
    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UserApp> getUserApps() {
        return userApps;
    }

    public void setUserApps(List<UserApp> userApps) {
        this.userApps = userApps;
    }

    // Méthodes utilitaires
    public void addUserApp(UserApp userApp) {
        userApps.add(userApp);
        userApp.setRole(this);
    }

    public void removeUserApp(UserApp userApp) {
        userApps.remove(userApp);
        userApp.setRole(null);
    }
}