package com.example.jeuxolympiques.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    // Peut être null dans certains cas (erreur système sans utilisateur connecté)
    @ManyToOne
    @JoinColumn(name = "user_app_id")
    private UserApp userApp;

    @NotNull(message = "L'action est obligatoire")
    @Column(nullable = false)
    private String action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogSeverity severity;

    // Entité concernée (Reservation, Offer...)
    private String entityType;

    // ID de l'entité concernée
    private String entityId;

    @Column(columnDefinition = "TEXT")
    private String details;

    private String ipAddress;

    @NotNull(message = "L'horodatage est obligatoire")
    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Enum pour les catégories de logs
    public enum LogCategory {
        SECURITY, BUSINESS, TECHNICAL, USER_ACTION
    }

    // Enum pour les niveaux de sévérité
    public enum LogSeverity {
        INFO, WARNING, ERROR, CRITICAL
    }

    // Constructeur par défaut
    public Log() {
        this.timestamp = LocalDateTime.now(); // Initialisation automatique
    }

    // Getters et Setters
    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long id) {
        this.logId = id;
    }

    public UserApp getUserApp() {
        return userApp;
    }

    public void setUserApp(UserApp userApp) {
        this.userApp = userApp;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LogCategory getCategory() {
        return category;
    }

    public void setCategory(LogCategory category) {
        this.category = category;
    }

    public LogSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(LogSeverity severity) {
        this.severity = severity;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}