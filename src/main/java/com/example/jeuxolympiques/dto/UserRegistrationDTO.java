package com.example.jeuxolympiques.dto;

import com.example.jeuxolympiques.model.UserApp;
import jakarta.validation.constraints.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

/**
 * DTO pour l'inscription d'un nouvel utilisateur
 */
public class UserRegistrationDTO {

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 50, message = "Le prénom ne doit pas dépasser 50 caractères")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50, message = "Le nom ne doit pas dépasser 50 caractères")
    private String lastName;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!*()_]).*$",
            message = "Le mot de passe doit contenir au moins un chiffre, une minuscule, une majuscule et un caractère spécial")
    private String password;

    @NotBlank(message = "La confirmation du mot de passe est obligatoire")
    private String confirmPassword;

    // Constructeur par défaut
    public UserRegistrationDTO() {
    }

    // Getters et setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    /**
     * Vérifie si les mots de passe correspondent
     * @return true si les mots de passe correspondent, false sinon
     */
    public boolean passwordsMatch() {
        return password != null && password.equals(confirmPassword);
    }

    /**
     * Convertit le DTO en entité UserApp
     * @param passwordEncoder Encodeur de mot de passe pour sécuriser le mot de passe
     * @return Une nouvelle instance de UserApp avec les données du DTO
     */
    public UserApp toEntity(PasswordEncoder passwordEncoder) {
        UserApp user = new UserApp();
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        user.setEmail(this.email);
        user.setPassword(passwordEncoder.encode(this.password));
        return user;
    }
}