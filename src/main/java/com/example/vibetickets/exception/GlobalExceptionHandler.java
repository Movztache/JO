package com.example.vibetickets.exception;

import com.example.vibetickets.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire global d'exceptions pour l'application
 * Intercepte les exceptions spécifiques et renvoie des réponses appropriées
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final LogService logService;

    @Autowired
    public GlobalExceptionHandler(LogService logService) {
        this.logService = logService;
    }

    /**
     * Gère les exceptions lorsqu'une entité n'est pas trouvée
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, String>> handleEntityNotFoundException(EntityNotFoundException ex) {
        logService.error("Ressource non trouvée: " + ex.getMessage(), ex);

        Map<String, String> errors = new HashMap<>();
        errors.put("error", "Ressource non trouvée");
        errors.put("message", ex.getMessage());

        return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
    }

    /**
     * Gère les erreurs de validation des données d'entrée
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logService.error("Erreur de validation: " + ex.getMessage(), ex);

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gère les accès non autorisés
     */
    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Map<String, String>> handleSecurityException(SecurityException ex) {
        logService.error("Accès non autorisé: " + ex.getMessage(), ex);

        Map<String, String> errors = new HashMap<>();
        errors.put("error", "Accès refusé");
        errors.put("message", ex.getMessage());

        return new ResponseEntity<>(errors, HttpStatus.FORBIDDEN);
    }

    /**
     * Gère toutes les autres exceptions non spécifiquement traitées
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, String>> handleGlobalException(Exception ex) {
        logService.error("Erreur interne du serveur: " + ex.getMessage(), ex);

        Map<String, String> errors = new HashMap<>();
        errors.put("error", "Erreur interne du serveur");
        errors.put("message", "Une erreur inattendue s'est produite. Veuillez réessayer plus tard.");

        return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}