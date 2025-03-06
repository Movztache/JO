package com.example.jeuxolympiques.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity // Cette annotation active la configuration de sécurité web de Spring
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Configuration des règles de sécurité HTTP
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/profile/**").authenticated() // Les URLs commençant par /profile/ nécessitent une authentification
                        .requestMatchers("/buy-ticket/**").authenticated() // Les URLs commençant par /buy-ticket/ nécessitent une authentication
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Les URLs commençant par /admin/ nécessitent le rôle "ADMIN".
                        .anyRequest().permitAll() // Toutes les autres requêtes sont permises sans authentification
                )
                .formLogin(withDefaults()); // Configure le formulaire de login avec les paramètres par défaut

        return http.build(); // Construit et retourne la chaîne de filtres de sécurité
    }
    @Bean
    public UserDetailsService userDetailsService() {
        // Configuration des utilisateurs en mémoire et de leurs rôles
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User.withUsername("admin")
                .password(passwordEncoder().encode("adminPass")) // Encode le mot de passe avec BCrypt
                .roles("ADMIN") // Assigne le rôle "ADMIN" à l'utilisateur
                .build());
        manager.createUser(User.withUsername("user")
                .password(passwordEncoder().encode("userPass")) // Encode le mot de passe avec BCrypt
                .roles("USER") // Assigne le rôle "USER" à l'utilisateur
                .build());
        return manager; // Retourne le gestionnaire d'utilisateurs
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Définit un bean pour encoder les mots de passe en utilisant BCrypt
        return new BCryptPasswordEncoder();
    }
}