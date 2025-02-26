package com.example.jeuxolympiques.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_app_id", nullable = false)
    private UserApp userApp;

    private String action;
    private LocalDateTime timestamp;

}
