package com.example.vibetickets.controller;

import com.example.vibetickets.model.Role;
import com.example.vibetickets.repository.RoleRepository;
import com.example.vibetickets.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RoleController {

    private final RoleService roleService;
    private final RoleRepository roleRepository;

    @Autowired
    public RoleController(RoleService roleService, RoleRepository roleRepository) {
        this.roleService = roleService;
        this.roleRepository = roleRepository;
    }

    /**
     * Récupère tous les rôles ou recherche des rôles par nom
     * @param name Le nom à rechercher (optionnel)
     * @return La liste des rôles correspondants
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<Role>> getAllRoles(@RequestParam(required = false) String name) {
        List<Role> roles;
        if (name != null && !name.isEmpty()) {
            // Si un nom est fourni, rechercher les rôles par nom
            roles = roleRepository.findByNameContainingIgnoreCase(name);
        } else {
            // Sinon, récupérer tous les rôles
            roles = roleService.findAllRoles();
        }
        return ResponseEntity.ok(roles);
    }

    /**
     * Récupère un rôle par son ID
     * @param roleId L'ID du rôle
     * @return Le rôle s'il existe
     */
    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> getRoleById(@PathVariable Long roleId) {
        try {
            Optional<Role> role = roleService.findById(roleId);
            if (role.isPresent()) {
                return ResponseEntity.ok(role.get());
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Rôle non trouvé avec l'ID : " + roleId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Erreur lors de la récupération du rôle");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Crée un nouveau rôle
     * @param role Le rôle à créer
     * @return Le rôle créé
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> createRole(@Valid @RequestBody Role role) {
        try {
            Role createdRole = roleService.createRole(role);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Erreur lors de la création du rôle");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Met à jour un rôle existant
     * @param roleId L'ID du rôle à mettre à jour
     * @param role Les nouvelles données du rôle
     * @return Le rôle mis à jour
     */
    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> updateRole(@PathVariable Long roleId, @Valid @RequestBody Role role) {
        try {
            Role updatedRole = roleService.updateRole(roleId, role);
            return ResponseEntity.ok(updatedRole);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Erreur lors de la mise à jour du rôle");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Supprime un rôle
     * @param roleId L'ID du rôle à supprimer
     * @return Un message de confirmation
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteRole(@PathVariable Long roleId) {
        try {
            boolean deleted = roleService.deleteRole(roleId);
            if (deleted) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Rôle supprimé avec succès");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Rôle non trouvé avec l'ID : " + roleId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Erreur lors de la suppression du rôle");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
