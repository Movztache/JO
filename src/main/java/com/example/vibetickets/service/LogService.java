package com.example.vibetickets.service;

import java.util.List;

public interface LogService {

    /**
     * Enregistre une information générale
     */
    void info(String message);

    /**
     * Enregistre un message d'avertissement
     */
    void warning(String message);

    /**
     * Enregistre une erreur
     */
    void error(String message, Throwable exception);

    /**
     * Journalise une action utilisateur avec son identifiant
     */
    void userAction(Long userId, String action);

    /**
     * Récupère les derniers logs (limité aux 50 plus récents)
     */
    List<String> getRecentLogs();
}