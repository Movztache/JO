package com.example.vibetickets.repository;

import com.example.vibetickets.model.Log;
import com.example.vibetickets.model.UserApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {

    // Recherche par utilisateur
    List<Log> findByUserApp(UserApp userApp);

    // Recherche par catégorie
    List<Log> findByCategory(Log.LogCategory category);

    // Recherche par sévérité
    List<Log> findBySeverity(Log.LogSeverity severity);

    // Recherche par période
    List<Log> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    // Récupérer les logs les plus récents
    List<Log> findTop20ByOrderByTimestampDesc();
}