// Implémentation du service
package com.example.jeuxolympiques.service.impl;

import com.example.jeuxolympiques.dto.UserRegistrationDTO;
import com.example.jeuxolympiques.model.Role;
import com.example.jeuxolympiques.model.UserApp;
import com.example.jeuxolympiques.repository.RoleRepository;
import com.example.jeuxolympiques.repository.UserAppRepository;
import com.example.jeuxolympiques.service.UserAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class UserAppServiceImpl implements UserAppService {

    private final UserAppRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private static final int KEY_LENGTH = 32;
    private static final int MAX_GENERATION_ATTEMPTS = 5;

    @Autowired
    public UserAppServiceImpl(UserAppRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserApp registerNewUser(@Valid UserRegistrationDTO dto) {
        // Vérification que les mots de passe correspondent
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas");
        }

        // Vérification que l'email n'est pas déjà utilisé
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        UserApp user = new UserApp();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setUserKey(generateUniqueUserKey());

        return userRepository.save(user);
    }

    @Override
    public UserApp findByUserKey(String userKey) {
        Optional<UserApp> user = userRepository.findByUserKey(userKey);
        return user.orElse(null);
    }

    private String generateRandomKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[KEY_LENGTH];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    public String generateUniqueUserKey() {
        String key;
        int attempts = 0;

        do {
            // Limiter le nombre de tentatives pour éviter une boucle infinie
            if (attempts >= MAX_GENERATION_ATTEMPTS) {
                throw new RuntimeException("Impossible de générer une clé unique après " + MAX_GENERATION_ATTEMPTS + " tentatives");
            }

            key = generateRandomKey();
            attempts++;
        } while (userRepository.existsByUserKey(key));

        return key;
    }

    @Override
    @Transactional
    public UserApp renewUserKey(Long userId) {
        UserApp user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID : " + userId));

        user.setUserKey(generateUniqueUserKey());
        return userRepository.save(user);
    }

    @Override
    public boolean validateUserKey(Long userId, String providedKey) {
        return userRepository.findById(userId)
                .map(user -> user.getUserKey().equals(providedKey))
                .orElse(false);
    }

    @Override
    public UserApp findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Met à jour les informations d'un utilisateur existant
     * @param user L'utilisateur avec les informations mises à jour
     * @return L'utilisateur mis à jour
     */
    @Override
    @Transactional
    public UserApp updateUser(UserApp user) {
        // Vérifier que l'utilisateur existe
        if (user.getUserId() == null || !userRepository.existsById(user.getUserId())) {
            throw new IllegalArgumentException("Utilisateur non trouvé avec l'ID : " + user.getUserId());
        }

        // Récupération de l'utilisateur existant pour vérifier si l'email est changé
        if (user.getEmail() != null) {
            UserApp existingUserWithEmail = userRepository.findByEmail(user.getEmail());
            if (existingUserWithEmail != null && !existingUserWithEmail.getUserId().equals(user.getUserId())) {
                throw new IllegalArgumentException("Cette adresse email est déjà utilisée par un autre utilisateur");
            }
        }

        // Sauvegarder les modifications
        return userRepository.save(user);
    }

    @Override
    public List<UserApp> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserApp findById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    @Transactional
    public UserApp updateUserRole(Long userId, Long roleId) {
        UserApp user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID : " + userId));

        // Si roleId est null, on supprime le rôle
        if (roleId == null) {
            user.setRole(null);
        } else {
            // Vérifier que le rôle existe
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Rôle non trouvé avec l'ID : " + roleId));

            // Assigner le rôle à l'utilisateur
            user.setRole(role);
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public boolean deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            return false;
        }

        userRepository.deleteById(userId);
        return true;
    }
}