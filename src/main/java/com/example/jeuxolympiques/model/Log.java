package com.example.jeuxolympiques.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Entity
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne
    @JoinColumn(name = "user_app_id", nullable = false)
    @NotNull(message = "L'utilisateur est obligatoire")
    private UserApp userApp;


    private String action;

    @NotNull()
    private LocalDateTime timestamp;


    public Long getLogId() {
        return logId;
    }
    public void setLogId(Long id) {
        this.logId = id;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setUserApp(UserApp userApp) {
        this.userApp = userApp;
    }
}
