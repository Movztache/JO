package com.example.vibetickets.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Filtre d'authentification JWT qui intercepte chaque requête HTTP pour vérifier
 * si elle contient un token JWT valide et authentifie l'utilisateur en conséquence.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    // Utilitaire pour manipuler les tokens JWT
    private final JwtUtils jwtUtils;

    // Service pour charger les détails des utilisateurs depuis la base de données
    private final UserDetailsService userDetailsService;

    /**
     * Constructeur qui injecte les dépendances nécessaires
     * @param jwtUtils Utilitaire JWT pour valider et analyser les tokens
     * @param userDetailsService Service pour charger les informations utilisateur
     */
    public JwtAuthFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Méthode principale du filtre exécutée pour chaque requête HTTP
     * @param request La requête HTTP entrante
     * @param response La réponse HTTP sortante
     * @param filterChain La chaîne de filtres pour continuer le traitement
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Extraire le token JWT de l'en-tête de la requête
            String jwt = parseJwt(request);

            // Vérifier si un token est présent et si aucune authentification n'existe déjà dans le contexte
            if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Extraire le nom d'utilisateur du token
                String username = jwtUtils.extractUsername(jwt);

                // Charger les détails de l'utilisateur à partir du nom d'utilisateur
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Valider le token avec les détails utilisateur
                if (jwtUtils.validateToken(jwt, userDetails)) {
                    // Créer un objet d'authentification Spring Security
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,       // Principal (utilisateur authentifié)
                                    null,              // Identifiants (non nécessaires car déjà authentifié)
                                    userDetails.getAuthorities());  // Autorisations/rôles de l'utilisateur

                    // Ajouter des détails supplémentaires sur l'authentification (comme l'adresse IP)
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    // Définir l'authentification dans le contexte de sécurité Spring
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            // Journaliser les erreurs d'authentification sans interrompre la chaîne de filtres
            logger.error("Impossible de définir l'authentification utilisateur: {}", e);
        }

        // Continuer l'exécution de la chaîne de filtres
        filterChain.doFilter(request, response);
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // Utiliser la méthode debug avec un seul paramètre de message
        logger.debug("Chemin de la requête: " + path);

        boolean shouldSkip = path.startsWith("/api/auth/") ||
                path.equals("/") ||
                path.equals("/login") ||
                path.startsWith("/home") ||
                path.startsWith("/offers/") ||
                path.equals("/register") ||
                path.startsWith("/error");

        // Utiliser la méthode debug avec un seul paramètre de message
        logger.debug("Requête exemptée du filtre JWT: " + shouldSkip);

        return shouldSkip;
    }

    /**
     * Extrait le token JWT de l'en-tête Authorization de la requête
     * @param request La requête HTTP
     * @return Le token JWT sans le préfixe "Bearer " ou null si non présent
     */
    private String parseJwt(HttpServletRequest request) {
        // Récupérer l'en-tête Authorization
        String headerAuth = request.getHeader("Authorization");

        // Vérifier si l'en-tête existe et commence par "Bearer "
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            // Extraire le token en supprimant le préfixe "Bearer "
            return headerAuth.substring(7);
        }

        return null;
    }
}