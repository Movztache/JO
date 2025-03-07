package com.example.jeuxolympiques.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.jeuxolympiques.model.UserApp;
import com.example.jeuxolympiques.repository.UserAppRepository;

@Service
public class UserService {

    private final UserAppRepository userAppRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserAppRepository userAppRepository, PasswordEncoder passwordEncoder) {
        this.userAppRepository = userAppRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserApp createUser(UserApp user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Assigner le rôle par défaut "UTILISATEUR"
        // Note: Si vous avez une table Role, vous pouvez la lier ici
        return userAppRepository.save(user);
    }
}