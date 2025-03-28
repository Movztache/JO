package com.example.jeuxolympiques.service.impl;

import com.example.jeuxolympiques.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Service
public class LogServiceImpl implements LogService {

    private static final Logger logger = LoggerFactory.getLogger(LogServiceImpl.class);
    private static final int MAX_LOGS = 50;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Liste en mémoire pour stocker les logs récents
    private final LinkedList<String> recentLogs = new LinkedList<>();

    @Override
    public void info(String message) {
        logger.info(message);
        addToRecentLogs("INFO", message);
    }

    @Override
    public void warning(String message) {
        logger.warn(message);
        addToRecentLogs("WARN", message);
    }

    @Override
    public void error(String message, Throwable exception) {
        logger.error(message, exception);
        addToRecentLogs("ERROR", message + (exception != null ? " - " + exception.getMessage() : ""));
    }

    @Override
    public void userAction(Long userId, String action) {
        String message = "Utilisateur " + userId + " a effectué: " + action;
        logger.info(message);
        addToRecentLogs("ACTION", message);
    }

    @Override
    public List<String> getRecentLogs() {
        return Collections.unmodifiableList(recentLogs);
    }

    private synchronized void addToRecentLogs(String level, String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String formattedLog = timestamp + " [" + level + "] " + message;

        // Utilisation de addFirst() au lieu de add(0, ...)
        recentLogs.addFirst(formattedLog);

        // Limitation de la taille
        if (recentLogs.size() > MAX_LOGS) {
            recentLogs.removeLast();
        }
    }
}