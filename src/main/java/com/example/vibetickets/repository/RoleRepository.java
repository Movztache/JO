package com.example.vibetickets.repository;

import com.example.vibetickets.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    /**
     * Trouve un rôle par son nom
     * @param name Le nom du rôle
     * @return Le rôle s'il existe, null sinon
     */
    @Query("SELECT r FROM Role r WHERE r.name = :name")
    Role findByName(@Param("name") String name);

    /**
     * Trouve tous les rôles dont le nom contient la chaîne spécifiée (insensible à la casse)
     * @param name La chaîne à rechercher dans le nom du rôle
     * @return La liste des rôles correspondants
     */
    @Query("SELECT r FROM Role r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Role> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Vérifie si un rôle avec le nom spécifié existe déjà
     * @param name Le nom du rôle
     * @return true si un rôle avec ce nom existe, false sinon
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Role r WHERE r.name = :name")
    boolean existsByName(@Param("name") String name);
}
