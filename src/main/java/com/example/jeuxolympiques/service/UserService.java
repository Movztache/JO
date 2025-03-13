package com.example.jeuxolympiques.service;

import com.example.jeuxolympiques.model.Rule;
import com.example.jeuxolympiques.repository.RuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.jeuxolympiques.model.UserApp;
import com.example.jeuxolympiques.repository.UserAppRepository;

import java.util.UUID;

@Service

public class UserService {

    private final UserAppRepository userAppRepository;
    private final PasswordEncoder passwordEncoder;
    private final RuleRepository ruleRepository;

    @Autowired
    public UserService(UserAppRepository userAppRepository, PasswordEncoder passwordEncoder, RuleRepository ruleRepository) {
        this.userAppRepository = userAppRepository;
        this.passwordEncoder = passwordEncoder;
        this.ruleRepository = ruleRepository;
    }

//    public void createUser(UserApp userApp) {
//        // Rechercher la règle "User" dans le contexte
//        Rule rule = ruleRepository.findByName("User");
//        if (rule != null) {
//            // Utiliser la règle pour attribuer des droits à l'utilisateur
//            UserApp.setRule((Rule) Collections.singletonList(rule));
//            // Créer une clé d'user avec UUID
//            String userKey = UUID.randomUUID().toString();
//            String hashedPassword = passwordEncoder.encode(userApp.getPassword());
//            userApp.setPassword(hashedPassword);
//            // Ajouter l'utilisateur au repository UserAppRepository
//            userAppRepository.save(userApp);
//
//        }
//    }
public void createUser(UserApp userApp) {
    // Rechercher la règle "User" dans le contexte
    Rule rule = ruleRepository.findByName("User");
    if (rule != null) {
        // Utiliser la règle pour attribuer des droits à l'utilisateur
        userApp.setRule(rule);

        // Créer une clé d'user avec UUID
        String userKey = UUID.randomUUID().toString();
        userApp.setUserKey(userKey);

        String hashedPassword = passwordEncoder.encode(userApp.getPassword());
        userApp.setPassword(hashedPassword);

        // Ajouter l'utilisateur au repository UserAppRepository
        userAppRepository.save(userApp);
    }
}


}
