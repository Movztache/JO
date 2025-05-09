package com.example.jeuxolympiques.model;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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


    public Long getRoleId() {
        return roleId;
    }
    public void setRoleId(Long id) {
        this.roleId = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

}
