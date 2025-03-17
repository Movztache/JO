package com.example.jeuxolympiques;

import com.example.jeuxolympiques.dto.UserRegistrationDTO;
import com.example.jeuxolympiques.model.UserApp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserRegistrationDTOTest {

    private UserRegistrationDTO dto;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        dto = new UserRegistrationDTO();
        dto.setFirstName("Jean");
        dto.setLastName("Dupont");
        dto.setEmail("jean.dupont@example.com");
        dto.setPassword("Password1@");
        dto.setConfirmPassword("Password1@");

        passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
    }

    @Test
    void passwordsMatch_WithMatchingPasswords_ShouldReturnTrue() {
        // Given - déjà configuré dans setUp

        // When
        boolean result = dto.passwordsMatch();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void passwordsMatch_WithMismatchedPasswords_ShouldReturnFalse() {
        // Given
        dto.setConfirmPassword("DifferentPassword1@");

        // When
        boolean result = dto.passwordsMatch();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void toEntity_ShouldCreateUserAppWithCorrectValues() {
        // Given - déjà configuré dans setUp

        // When
        UserApp user = dto.toEntity(passwordEncoder);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getFirstName()).isEqualTo("Jean");
        assertThat(user.getLastName()).isEqualTo("Dupont");
        assertThat(user.getEmail()).isEqualTo("jean.dupont@example.com");
        assertThat(user.getPassword()).isEqualTo("encoded_password");
        assertThat(user.getUserKey()).isNotNull().isNotEmpty();
    }

    @Test
    void toEntity_ShouldGenerateUserKey() {
        // Given - déjà configuré dans setUp

        // When
        UserApp user = dto.toEntity(passwordEncoder);

        // Then
        assertThat(user.getUserKey()).isNotNull().isNotEmpty();
        // Vérifier que c'est un UUID au format correct (36 caractères avec des tirets)
        assertThat(user.getUserKey()).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    @Test
    void toEntity_ShouldEncodePassword() {
        // Given
        PasswordEncoder realEncoder = new BCryptPasswordEncoder();

        // When
        UserApp user = dto.toEntity(realEncoder);

        // Then
        // Vérifier que le mot de passe a bien été encodé (ne correspond pas au mot de passe en clair)
        assertThat(user.getPassword()).isNotEqualTo("Password1@");
        // Vérifier que l'encodeur peut vérifier le mot de passe
        assertThat(realEncoder.matches("Password1@", user.getPassword())).isTrue();
    }
}