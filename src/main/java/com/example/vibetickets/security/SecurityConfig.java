package com.example.vibetickets.security;

import com.example.vibetickets.service.CustomUserDetailsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration de la sécurité de l'application
 * Gère l'authentification, l'autorisation et les erreurs de sécurité
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthFilter jwtAuthFilter;
    private final Environment environment;

    /**
     * Constructeur pour l'injection de dépendances
     */
    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtAuthFilter jwtAuthFilter, Environment environment) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
        this.environment = environment;
    }

    /**
     * Configure la chaîne de filtres de sécurité et les règles d'accès
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Désactivation de CSRF pour les API REST
                .csrf(AbstractHttpConfigurer::disable)

                // Configuration des règles d'autorisation pour différents chemins d'URL
                .authorizeHttpRequests(authorize -> authorize
                        // PRIORITÉ ABSOLUE: Routes d'authentification DOIVENT être accessibles sans auth
                        .requestMatchers("/api/authentication/**").permitAll()
                        // Routes publiques accessibles sans authentification
                        .requestMatchers("/", "/home", "/offers/**", "/register", "/api/offers/**","/api/cart/**").permitAll()
                        // Permettre l'accès aux pages d'erreur pour tous les utilisateurs
                        .requestMatchers("/error", "/error/**").permitAll()
                        // Routes nécessitant une authentification
                        .requestMatchers("/profile/**").authenticated()
                        .requestMatchers("/buy-ticket/**").authenticated()
                        .requestMatchers("/api/reservations/**").authenticated()
                        .requestMatchers("/api/tickets/**").authenticated()
                        .requestMatchers("/api/utilisateurs/**").authenticated()
                        // Routes nécessitant un rôle spécifique
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                         .anyRequest().authenticated()
                )

                // Gestion des exceptions de sécurité avec des gestionnaires personnalisés
                .exceptionHandling(exceptions -> exceptions
                        // Gestionnaire pour les requêtes non authentifiées (401 UNAUTHORIZED)
                        .authenticationEntryPoint(customAuthenticationEntryPoint())
                        // Gestionnaire pour les accès refusés (403 FORBIDDEN)
                        .accessDeniedHandler(customAccessDeniedHandler())
                )

                // Configuration du formulaire de connexion pour l'interface web
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .defaultSuccessUrl("/home")
                        .failureUrl("/login?error")
                )

                // Configuration de la déconnexion
                .logout(logout -> logout
                        .permitAll()
                        .logoutSuccessUrl("/")
                )

                // Configuration pour JWT : session stateless (sans état)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Définition du fournisseur d'authentification
                .authenticationProvider(authenticationProvider())

                // Ajout du filtre JWT avant le filtre standard d'authentification
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Récupérer les origines autorisées depuis la configuration
        String corsOriginsStr = environment.getProperty("cors.allowed.origins", "http://localhost:4200");
        List<String> corsOrigins = Arrays.asList(corsOriginsStr.split(","));

        // Configuration CORS
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedOrigins(corsOrigins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Crée un point d'entrée d'authentification personnalisé pour gérer les erreurs 401 UNAUTHORIZED
     * Ce gestionnaire est appelé quand un utilisateur non authentifié essaie d'accéder à une ressource protégée
     */
    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return new AuthenticationEntryPoint() {
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response,
                                 AuthenticationException authException) throws IOException, ServletException {

                // Si c'est une requête API, retourner une réponse JSON formatée
                if (isApiRequest(request)) {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"error\":\"Non authentifié\",\"status\":401,\"message\":\""
                            + authException.getMessage() + "\"}");
                } else {
                    // Pour les requêtes web traditionnelles, rediriger vers la page de connexion avec un message d'erreur
                    response.sendRedirect("/login?unauthorized");
                }
            }
        };
    }

    /**
     * Crée un gestionnaire d'accès refusé personnalisé pour gérer les erreurs 403 FORBIDDEN
     * Ce gestionnaire est appelé quand un utilisateur authentifié tente d'accéder à une ressource
     * pour laquelle il n'a pas les autorisations nécessaires
     */
    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return new AccessDeniedHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response,
                               AccessDeniedException accessDeniedException) throws IOException, ServletException {

                // Si c'est une requête API, retourner une réponse JSON formatée
                if (isApiRequest(request)) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"error\":\"Accès refusé\",\"status\":403,\"message\":\""
                            + accessDeniedException.getMessage() + "\"}");
                } else {
                    // Pour les requêtes web traditionnelles, rediriger vers une page d'erreur 403 personnalisée
                    response.sendRedirect("/error/403");
                }
            }
        };
    }

    /**
     * Méthode utilitaire pour détecter si une requête est une requête API
     * Une requête est considérée comme API si:
     * - Elle a un en-tête "Accept: application/json"
     * - OU si l'URL commence par "/api/"
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String acceptHeader = request.getHeader("Accept");
        String path = request.getRequestURI();

        return (acceptHeader != null && acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE))
                || (path != null && path.startsWith("/api/"));
    }

    /**
     * Configure le fournisseur d'authentification basé sur DAO
     * Ce fournisseur utilise le service CustomUserDetailsService pour charger les détails de l'utilisateur
     * et l'encodeur de mot de passe pour valider les mots de passe
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Configure le gestionnaire d'authentification
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Définit l'algorithme d'encodage des mots de passe
     * BCrypt est recommandé pour sa sécurité et son adaptabilité
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}