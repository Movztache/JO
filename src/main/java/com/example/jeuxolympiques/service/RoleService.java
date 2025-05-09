package com.example.jeuxolympiques.service;

import com.example.jeuxolympiques.model.Role;
import java.util.List;
import java.util.Optional;

public interface RoleService {

    /**
     * Récupère tous les rôles
     * @return La liste de tous les rôles
     */
    List<Role> findAllRoles();

    /**
     * Trouve un rôle par son ID
     * @param roleId L'ID du rôle
     * @return Le rôle s'il existe
     */
    Optional<Role> findById(Long roleId);

    /**
     * Trouve un rôle par son nom
     * @param name Le nom du rôle
     * @return Le rôle s'il existe
     */
    Role findByName(String name);

    /**
     * Crée un nouveau rôle
     * @param role Le rôle à créer
     * @return Le rôle créé
     */
    Role createRole(Role role);

    /**
     * Met à jour un rôle existant
     * @param roleId L'ID du rôle à mettre à jour
     * @param role Les nouvelles données du rôle
     * @return Le rôle mis à jour
     */
    Role updateRole(Long roleId, Role role);

    /**
     * Supprime un rôle
     * @param roleId L'ID du rôle à supprimer
     * @return true si le rôle a été supprimé, false sinon
     */
    boolean deleteRole(Long roleId);
}

