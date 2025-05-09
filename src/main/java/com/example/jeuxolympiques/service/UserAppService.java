// Interface du service
package com.example.jeuxolympiques.service;

import com.example.jeuxolympiques.dto.UserRegistrationDTO;
import com.example.jeuxolympiques.model.UserApp;

import jakarta.validation.Valid;

import java.util.List;

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

    /**
     * Récupère tous les utilisateurs
     * @return La liste de tous les utilisateurs
     */
    List<UserApp> findAllUsers();

    /**
     * Trouve un utilisateur par son ID
     * @param userId L'ID de l'utilisateur
     * @return L'utilisateur ou null si aucun utilisateur n'est trouvé
     */
    UserApp findById(Long userId);

    /**
     * Met à jour le rôle d'un utilisateur
     * @param userId L'ID de l'utilisateur
     * @param roleId L'ID du rôle
     * @return L'utilisateur mis à jour
     */
    UserApp updateUserRole(Long userId, Long roleId);

    /**
     * @deprecated Utiliser {@link #updateUserRole(Long, Long)} à la place
     */
    @Deprecated
    default UserApp updateUserRule(Long userId, Long roleId) {
        return updateUserRole(userId, roleId);
    }

    /**
     * Supprime un utilisateur
     * @param userId L'ID de l'utilisateur à supprimer
     * @return true si l'utilisateur a été supprimé, false sinon
     */
    boolean deleteUser(Long userId);
}