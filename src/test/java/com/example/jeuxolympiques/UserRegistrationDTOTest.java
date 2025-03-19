package com.example.jeuxolympiques;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.jeuxolympiques.model.UserApp;
import com.example.jeuxolympiques.dto.UserRegistrationDTO;

public class UserRegistrationDTOTest {

    private UserRegistrationDTO userRegistrationDTO;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRegistrationDTO = new UserRegistrationDTO();
        userRegistrationDTO.setFirstName("Thomas");
        userRegistrationDTO.setLastName("Dupont");
        userRegistrationDTO.setEmail("thomas.dupont@example.com");
        userRegistrationDTO.setPassword("Azerty123!");
        userRegistrationDTO.setConfirmPassword("Azerty123!");

        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Test
    void testPasswordsMatch_WhenPasswordsAreEqual() {
        assertTrue(userRegistrationDTO.passwordsMatch(),
                "Les mots de passe identiques devraient renvoyer true");
    }

    @Test
    void testPasswordsMatch_WhenPasswordsAreNotEqual() {
        userRegistrationDTO.setConfirmPassword("DifferentPassword123!");
        assertFalse(userRegistrationDTO.passwordsMatch(),
                "Les mots de passe différents devraient renvoyer false");
    }

    @Test
    void testPasswordsMatch_WhenPasswordIsNull() {
        userRegistrationDTO.setPassword(null);
        assertFalse(userRegistrationDTO.passwordsMatch(),
                "Un mot de passe null devrait renvoyer false");
    }

    @Test
    void testToEntity_CreatesUserAppWithCorrectData() {
        UserApp userApp = userRegistrationDTO.toEntity(passwordEncoder);

        assertEquals("Thomas", userApp.getFirstName(), "Le prénom devrait être correctement transféré");
        assertEquals("Dupont", userApp.getLastName(), "Le nom devrait être correctement transféré");
        assertEquals("thomas.dupont@example.com", userApp.getEmail(), "L'email devrait être correctement transféré");
        assertNotNull(userApp.getPassword(), "Le mot de passe ne devrait pas être null");
        assertTrue(passwordEncoder.matches("Azerty123!", userApp.getPassword()),
                "Le mot de passe devrait être correctement encodé");
    }

    @Test
    void testToEntity_EncodesPassword() {
        UserApp userApp = userRegistrationDTO.toEntity(passwordEncoder);

        // Vérifie que le mot de passe a été encodé (différent du mot de passe d'origine)
        assertNotEquals("Azerty123!", userApp.getPassword(),
                "Le mot de passe stocké devrait être encodé et différent de l'original");

        // Vérifie que l'encodage fonctionne correctement
        assertTrue(passwordEncoder.matches("Azerty123!", userApp.getPassword()),
                "Le mot de passe encodé devrait être vérifiable avec le mot de passe original");
    }

    @Test
    void testGettersAndSetters() {
        UserRegistrationDTO dto = new UserRegistrationDTO();

        dto.setFirstName("Pierre");
        assertEquals("Pierre", dto.getFirstName(), "Le getter firstName devrait retourner la valeur définie");

        dto.setLastName("Martin");
        assertEquals("Martin", dto.getLastName(), "Le getter lastName devrait retourner la valeur définie");

        dto.setEmail("pierre.martin@example.com");
        assertEquals("pierre.martin@example.com", dto.getEmail(),
                "Le getter email devrait retourner la valeur définie");

        dto.setPassword("TestPassword123!");
        assertEquals("TestPassword123!", dto.getPassword(),
                "Le getter password devrait retourner la valeur définie");

        dto.setConfirmPassword("TestPassword123!");
        assertEquals("TestPassword123!", dto.getConfirmPassword(),
                "Le getter confirmPassword devrait retourner la valeur définie");
    }

    @Test
    void testConstructeurParDefaut() {
        UserRegistrationDTO dto = new UserRegistrationDTO();

        assertNull(dto.getFirstName(), "Le prénom devrait être null par défaut");
        assertNull(dto.getLastName(), "Le nom devrait être null par défaut");
        assertNull(dto.getEmail(), "L'email devrait être null par défaut");
        assertNull(dto.getPassword(), "Le mot de passe devrait être null par défaut");
        assertNull(dto.getConfirmPassword(), "La confirmation du mot de passe devrait être null par défaut");
    }
}