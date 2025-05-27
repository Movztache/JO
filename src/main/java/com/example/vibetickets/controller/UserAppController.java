package com.example.vibetickets.controller;

import com.example.vibetickets.model.UserApp;
import com.example.vibetickets.service.UserAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/utilisateurs")
public class UserAppController {

    private final UserAppService userAppService;

    @Autowired
    public UserAppController(UserAppService userAppService) {
        this.userAppService = userAppService;
    }

    /**
     * Convertit un objet UserApp en Map contenant uniquement les informations publiques
     * @param user L'utilisateur à convertir
     * @return Une Map contenant les données publiques de l'utilisateur
     */
    private Map<String, Object> convertUserToMap(UserApp user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getUserId());
        userMap.put("email", user.getEmail());
        userMap.put("firstName", user.getFirstName());
        userMap.put("lastName", user.getLastName());
        userMap.put("userKey", user.getUserKey());

        // Ajouter les informations sur le rôle si disponible
        if (user.getRole() != null) {
            userMap.put("roleId", user.getRole().getRoleId());
            userMap.put("roleName", user.getRole().getName());
        } else {
            userMap.put("roleId", null);
            userMap.put("roleName", null);
        }

        // Ne pas inclure le mot de passe ou d'autres données sensibles
        return userMap;
    }

    /**
     * Récupère le profil de l'utilisateur connecté
     * @return Les données du profil de l'utilisateur
     */
    @GetMapping("/profil")
    public ResponseEntity<?> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non authentifié");
        }

        try {
            // Récupérer les détails de l'utilisateur depuis l'authentification
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername(); // Dans Spring Security, username correspond à l'email

            // Récupérer l'utilisateur complet depuis le repository
            UserApp user = userAppService.findByEmail(email);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
            }

            // Utiliser la méthode helper pour convertir l'utilisateur en Map
            return ResponseEntity.ok(convertUserToMap(user));
        } catch (ClassCastException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération du profil: " + e.getMessage());
        }
    }

    /**
     * Renouvelle la clé API d'un utilisateur
     * @return La nouvelle clé générée
     */
    @PostMapping("/renouveler-cle")
    public ResponseEntity<?> renewUserKey() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non authentifié");
        }

        try {
            // Récupérer le nom d'utilisateur (email)
            String username = authentication.getName();

            if (authentication.getPrincipal() instanceof UserDetails) {
                username = ((UserDetails) authentication.getPrincipal()).getUsername();
            }

            // Rechercher l'utilisateur
            UserApp user = userAppService.findByEmail(username);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
            }

            // Renouveler la clé
            user = userAppService.renewUserKey(user.getUserId());

            // Préparer la réponse
            Map<String, Object> response = convertUserToMap(user);
            response.put("message", "Clé API renouvelée avec succès");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors du renouvellement de la clé API");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Met à jour les informations du profil de l'utilisateur connecté
     * @param userDetails Les nouvelles informations de l'utilisateur à mettre à jour
     * @return Les données mises à jour du profil de l'utilisateur
     */
    @PutMapping("/profil")
    public ResponseEntity<?> updateUserProfile(@RequestBody Map<String, String> userDetails) {
        try {
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Si l'authentification est basée sur UserDetails
            if (authentication.getPrincipal() instanceof UserDetails) {
                username = ((UserDetails) authentication.getPrincipal()).getUsername();
            }

            UserApp user = userAppService.findByEmail(username);

            if (user == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Utilisateur non trouvé");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            // Mettre à jour les informations qui sont présentes dans la requête
            boolean updated = false;

            if (userDetails.containsKey("firstName")) {
                user.setFirstName(userDetails.get("firstName"));
                updated = true;
            }

            if (userDetails.containsKey("lastName")) {
                user.setLastName(userDetails.get("lastName"));
                updated = true;
            }

            // Sauvegarder uniquement si des modifications ont été effectuées
            if (updated) {
                user = userAppService.updateUser(user);
            }

            // Utiliser la méthode helper pour créer la réponse
            Map<String, Object> response = convertUserToMap(user);
            response.put("message", "Profil mis à jour avec succès");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de la mise à jour du profil");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Récupère tous les utilisateurs
     * @return La liste de tous les utilisateurs
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserApp> users = userAppService.findAllUsers();
            List<Map<String, Object>> usersList = users.stream()
                    .map(this::convertUserToMap)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(usersList);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de la récupération des utilisateurs");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Récupère un utilisateur par son ID
     * @param userId L'ID de l'utilisateur
     * @return Les données de l'utilisateur
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            UserApp user = userAppService.findById(userId);
            if (user == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Utilisateur non trouvé");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            return ResponseEntity.ok(convertUserToMap(user));
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de la récupération de l'utilisateur");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Met à jour le rôle d'un utilisateur
     * @param userId L'ID de l'utilisateur
     * @param roleId L'ID du rôle
     * @return Les données mises à jour de l'utilisateur
     */
    @PutMapping("/{userId}/role/{roleId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> updateUserRole(@PathVariable Long userId, @PathVariable Long roleId) {
        try {
            UserApp user = userAppService.updateUserRole(userId, roleId);
            Map<String, Object> response = convertUserToMap(user);
            response.put("message", "Rôle de l'utilisateur mis à jour avec succès");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Utilisateur ou rôle non trouvé");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de la mise à jour du rôle de l'utilisateur");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Supprime le rôle d'un utilisateur
     * @param userId L'ID de l'utilisateur
     * @return Les données mises à jour de l'utilisateur
     */
    @DeleteMapping("/{userId}/role")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> removeUserRole(@PathVariable Long userId) {
        try {
            UserApp user = userAppService.updateUserRole(userId, null);
            Map<String, Object> response = convertUserToMap(user);
            response.put("message", "Rôle de l'utilisateur supprimé avec succès");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Utilisateur non trouvé");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de la suppression du rôle de l'utilisateur");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Supprime un utilisateur
     * @param userId L'ID de l'utilisateur à supprimer
     * @return Un message de confirmation
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            boolean deleted = userAppService.deleteUser(userId);
            if (!deleted) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Utilisateur non trouvé");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            Map<String, String> response = new HashMap<>();
            response.put("message", "Utilisateur supprimé avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de la suppression de l'utilisateur");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}