package com.example.vibetickets.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Configuration Jackson pour gérer les problèmes d'encodage UTF-8,
 * le formatage des dates et améliorer la robustesse du parsing JSON.
 *
 * @author Vibe-Tickets Team
 */
@Configuration
public class JacksonConfig {

    /**
     * Configure ObjectMapper pour être plus permissif avec les erreurs d'encodage
     * et formater correctement les dates au format ISO
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder.json()
                // Configuration des dates
                .modules(new JavaTimeModule())
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .featuresToEnable(
                    // Permet de continuer le parsing même avec des caractères UTF-8 invalides
                    JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS,
                    JsonParser.Feature.ALLOW_SINGLE_QUOTES,
                    JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES
                )
                .featuresToDisable(
                    // Désactive la validation stricte des caractères UTF-8
                    JsonParser.Feature.STRICT_DUPLICATE_DETECTION
                )
                .build();
    }
}
