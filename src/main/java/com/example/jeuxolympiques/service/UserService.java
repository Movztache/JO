package com.example.jeuxolympiques.service;

import com.example.jeuxolympiques.dto.UserRegistrationDTO;
import com.example.jeuxolympiques.model.UserApp;
import com.example.jeuxolympiques.repository.UserAppRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;

/**
 * Service qui gère les opérations liées aux utilisateurs
 */
@Service
public class UserService {

    private final UserAppRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserAppRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Enregistre un nouvel utilisateur à partir du DTO d'inscription
     * @param dto Les données d'inscription validées
     * @return L'entité utilisateur créée et sauvegardée
     */
    public UserApp registerNewUser(@Valid UserRegistrationDTO dto) {
        // Vérifie si les mots de passe correspondent
        if (!dto.passwordsMatch()) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas");
        }

        // Vérifie si l'email est déjà utilisé
        if (userRepository.findByEmail(dto.getEmail()) != null) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        // Convertit le DTO en entité et sauvegarde
        UserApp user = dto.toEntity(passwordEncoder);
        return userRepository.save(user);
    }
}