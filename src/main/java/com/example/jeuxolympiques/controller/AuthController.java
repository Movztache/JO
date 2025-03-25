package com.example.jeuxolympiques.controller;

import com.example.jeuxolympiques.dto.JwtResponse;
import com.example.jeuxolympiques.dto.LoginRequest;
import com.example.jeuxolympiques.dto.UserRegistrationDTO;
import com.example.jeuxolympiques.model.UserApp;
import com.example.jeuxolympiques.repository.UserAppRepository;
import com.example.jeuxolympiques.security.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur REST pour gérer l'authentification des utilisateurs
 * - Inscription (register)
 * - Connexion (login)
 */
@RestController
@RequestMapping("/api/auth")  // Préfixe de tous les endpoints d'authentification
@CrossOrigin(origins = "*", maxAge = 3600)  // Permet les requêtes cross-origin
public class AuthController {

    // Déclaration des dépendances nécessaires
    private final AuthenticationManager authenticationManager;  // Pour authentifier les utilisateurs
    private final UserAppRepository userRepository;  // Pour les opérations de base de données sur les utilisateurs
    private final PasswordEncoder passwordEncoder;  // Pour encoder les mots de passe
    private final JwtUtils jwtUtils;  // Pour générer et valider les tokens JWT

    /**
     * Constructeur pour l'injection de dépendances
     * Meilleure pratique que l'injection par champ (@Autowired)
     */
    public AuthController(
            AuthenticationManager authenticationManager,
            UserAppRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Endpoint d'authentification (connexion)
     *
     * @param loginRequest DTO contenant email et mot de passe
     * @return ResponseEntity avec JWT token et informations utilisateur
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Authentifie l'utilisateur avec Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        // Stocke l'authentification dans le contexte de sécurité
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Génère un token JWT pour l'utilisateur
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtils.generateToken(userDetails);  // Utilise generateToken au lieu de generateJwtToken

        // Récupère les détails de l'utilisateur connecté
        UserApp user = userRepository.findByEmail(userDetails.getUsername());
        if (user == null) {
            throw new RuntimeException("Utilisateur non trouvé avec l'email: " + userDetails.getUsername());
        }

        // Retourne le token JWT et les informations de base de l'utilisateur
        return ResponseEntity.ok(new JwtResponse(
                jwt,
                user.getUserId(),
                user.getEmail(),
                user.getRule() != null ? user.getRule().getName() : "ROLE_USER"));
    }

    /**
     * Endpoint d'inscription (création de compte)
     *
     * @param registrationDto DTO contenant les informations d'inscription
     * @return ResponseEntity avec message de succès ou d'erreur
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDTO registrationDto) {
        // Vérifier si les mots de passe correspondent
        if (!registrationDto.passwordsMatch()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Les mots de passe ne correspondent pas"));
        }

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Cet email est déjà utilisé"));
        }

        // Créer un nouvel utilisateur à partir du DTO
        // La méthode toEntity s'occupe de convertir le DTO en entité UserApp
        // et d'encoder le mot de passe
        UserApp user = registrationDto.toEntity(passwordEncoder);

        // Enregistrer l'utilisateur dans la base de données
        userRepository.save(user);

        // Retourner une réponse avec code 201 (CREATED)
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Utilisateur enregistré avec succès"));
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        // Création d'un simple objet Map qui sera converti en JSON
        Map<String, Object> response = new HashMap<>();
        response.put("message", "API fonctionne");
        response.put("timestamp", new Date().getTime());
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }


}