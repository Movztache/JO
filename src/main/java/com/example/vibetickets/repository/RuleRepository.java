package com.example.vibetickets.repository;

import com.example.vibetickets.model.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RuleRepository extends JpaRepository<Rule, Long> {
        // Exécuter une requête SQL pour récupérer les règles avec le nom spécifié
        @Query("SELECT r FROM Rule r WHERE r.name = :name")
        Rule findByName(@Param("name") String name);

}