// Interface du service
package com.example.vibetickets.service;

import com.example.vibetickets.dto.UserRegistrationDTO;
import com.example.vibetickets.model.UserApp;

import jakarta.validation.Valid;

public interface UserAppService {
    /**
     * Enregistre un nouvel utilisateur à partir du DTO d'inscription
     * @param dto Les données d'inscription validées
     * @return L'entité utilisateur créée et sauvegardée
     */
    UserApp registerNewUser(@Valid UserRegistrationDTO dto);

    /**
     * Trouve un utilisateur par sa clé unique
     * @param userKey La clé de l'utilisateur
     * @return L'utilisateur ou null si aucun utilisateur n'est trouvé
     */
    UserApp findByUserKey(String userKey);

    /**
     * Génère une clé utilisateur unique
     * @return La clé générée
     */
    String generateUniqueUserKey();

    /**
     * Renouvelle la clé d'un utilisateur existant
     * @param userId L'identifiant de l'utilisateur
     * @return L'utilisateur avec sa nouvelle clé
     */
    UserApp renewUserKey(Long userId);

    /**
     * Vérifie si la clé fournie correspond à celle de l'utilisateur
     * @param userId L'identifiant de l'utilisateur
     * @param providedKey La clé à vérifier
     * @return true si la clé correspond, false sinon
     */
    boolean validateUserKey(Long userId, String providedKey);

    /**
     * Trouve un utilisateur par son email
     * @param email L'email de l'utilisateur
     * @return L'utilisateur ou null si aucun utilisateur n'est trouvé
     */
    UserApp findByEmail(String email);

    /**
     * Met à jour les informations d'un utilisateur existant
     * @param user L'utilisateur avec les informations mises à jour
     * @return L'utilisateur mis à jour
     */
    UserApp updateUser(UserApp user);

}