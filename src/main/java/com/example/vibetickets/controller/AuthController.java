package com.example.vibetickets.controller;

import com.example.vibetickets.dto.JwtResponse;
import com.example.vibetickets.dto.LoginRequest;
import com.example.vibetickets.dto.UserRegistrationDTO;
import com.example.vibetickets.model.Rule;
import com.example.vibetickets.model.UserApp;
import com.example.vibetickets.repository.RuleRepository;
import com.example.vibetickets.repository.UserAppRepository;
import com.example.vibetickets.security.JwtUtils;
import com.example.vibetickets.service.UserAppService;
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

import java.util.*;

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
    private final UserAppService userAppService;
    private final RuleRepository ruleRepository;

    /**
     * Constructeur pour l'injection de dépendances
     * Meilleure pratique que l'injection par champ (@Autowired)
     */
    public AuthController(
            AuthenticationManager authenticationManager,
            UserAppRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils,
            UserAppService userAppService,
            RuleRepository ruleRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.userAppService = userAppService;
        this.ruleRepository = ruleRepository;
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
                user.getRule() != null ? user.getRule().getName() : "User"));
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

        // Création d'un nouvel utilisateur à partir du DTO
        UserApp newUser = new UserApp();
        newUser.setFirstName(registrationDto.getFirstName());
        newUser.setLastName(registrationDto.getLastName());
        newUser.setEmail(registrationDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        // Génération de la clé unique
        String userKey = userAppService.generateUniqueUserKey();
        newUser.setUserKey(userKey);

        // Attribution du rôle USER
        Rule userRule = ruleRepository.findByName("User");

        // Vérifier si le rôle existe
        if (userRule == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Erreur: Le rôle USER n'est pas trouvé dans le système."));
        }

        // Assigner le rôle directement à l'utilisateur
        newUser.setRule(userRule);

        // Enregistrement de l'utilisateur
        userRepository.save(newUser);

        // Retourner une réponse avec code 201 (CREATED)
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Utilisateur enregistré avec succès"));
    }

    /**
     * Vérifie si l'utilisateur a accès au panier avec la clé fournie
     * @param userId ID de l'utilisateur
     * @param userKey Clé d'accès utilisateur
     * @return Statut d'accès au panier
     */
    @GetMapping("/cart-access/{userId}/{userKey}")
    public ResponseEntity<Map<String, Boolean>> validateCartAccess(
            @PathVariable Long userId,
            @PathVariable String userKey) {

        boolean isValid = userAppService.validateUserKey(userId, userKey);

        Map<String, Boolean> response = new HashMap<>();
        response.put("accessGranted", isValid);

        if (isValid) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }

    /**
     * Accède au panier d'un utilisateur avec sa clé
     * @param userId ID de l'utilisateur
     * @param userKey Clé d'accès utilisateur
     * @return Le contenu du panier si l'accès est autorisé
     */
    @GetMapping("/cart/{userId}/{userKey}")
    public ResponseEntity<?> getCartWithKey(
            @PathVariable Long userId,
            @PathVariable String userKey) {

        if (!userAppService.validateUserKey(userId, userKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Clé utilisateur invalide"));
        }

        // Récupérer l'utilisateur et son panier en utilisant la clé (userKey)
        UserApp user = userAppService.findByUserKey(userKey);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Déléguer au service CartService pour récupérer le panier
        // Supposons qu'un objet CartService est injecté
        // List<CartItemDTO> cartItems = cartService.getCartItems(user);

        return ResponseEntity.ok(Map.of("message", "Accès au panier autorisé pour l'utilisateur " + userId));
    }


}