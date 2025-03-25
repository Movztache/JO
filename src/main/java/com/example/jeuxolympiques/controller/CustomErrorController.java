package com.example.jeuxolympiques.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur personnalisé pour gérer les erreurs HTTP
 * Traite différemment les requêtes API et les requêtes web traditionnelles
 */
@Controller
public class CustomErrorController implements ErrorController {

    // Constantes pour les codes d'état HTTP
    private static final int STATUS_NOT_FOUND = 404;
    private static final int STATUS_FORBIDDEN = 403;
    private static final int STATUS_UNAUTHORIZED = 401;
    private static final int STATUS_BAD_REQUEST = 400;
    private static final int STATUS_INTERNAL_SERVER_ERROR = 500;

    /**
     * Point d'entrée principal pour gérer toutes les erreurs
     * Différencie le traitement selon le type de requête et le code d'erreur
     */
    @RequestMapping("/error")
    public Object handleError(HttpServletRequest request, Model model) {
        // Récupérer le code d'état
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (statusCode == null) {
            statusCode = STATUS_INTERNAL_SERVER_ERROR;
        }

        // Si c'est une requête API, renvoyer une réponse JSON
        if (isApiRequest(request)) {
            return createJsonErrorResponse(request, statusCode);
        }

        // Pour les requêtes web, configurer le modèle et renvoyer la vue appropriée
        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorMessage", getErrorMessage(statusCode));

        // Sélectionner la page d'erreur appropriée
        if (statusCode == STATUS_NOT_FOUND) {
            return "error/404";
        } else if (statusCode == STATUS_FORBIDDEN) {
            return "error/403";
        } else if (statusCode == STATUS_INTERNAL_SERVER_ERROR) {
            return "error/500";
        } else {
            return "error/generic";
        }
    }

    /**
     * Crée une réponse d'erreur au format JSON pour les requêtes API
     */
    @ResponseBody
    private Map<String, Object> createJsonErrorResponse(HttpServletRequest request, int statusCode) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", statusCode);
        errorResponse.put("error", getErrorMessage(statusCode));
        errorResponse.put("path", request.getRequestURI());

        // Si ce n'est pas une erreur 500, ajoutez des détails supplémentaires
        if (statusCode != STATUS_INTERNAL_SERVER_ERROR) {
            String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
            if (errorMessage != null && !errorMessage.isEmpty()) {
                errorResponse.put("message", errorMessage);
            }
        }

        return errorResponse;
    }
    /**
     * Renvoie un message d'erreur en fonction du code d'état HTTP
     */
    private String getErrorMessage(int statusCode) {
        return switch (statusCode) {
            case STATUS_NOT_FOUND -> "Ressource non trouvée";
            case STATUS_FORBIDDEN -> "Accès refusé";
            case STATUS_UNAUTHORIZED -> "Non authentifié";
            case STATUS_BAD_REQUEST -> "Requête incorrecte";
            case STATUS_INTERNAL_SERVER_ERROR -> "Erreur interne du serveur";
            default -> "Erreur inattendue";
        };
    }

    /**
     * Détermine si la requête est une requête API
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String acceptHeader = request.getHeader("Accept");
        String path = request.getRequestURI();

        return (acceptHeader != null && acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE))
                || (path != null && path.startsWith("/api/"));
    }
}