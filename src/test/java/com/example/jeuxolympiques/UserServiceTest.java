package com.example.jeuxolympiques.service;

import com.example.jeuxolympiques.dto.UserRegistrationDTO;
import com.example.jeuxolympiques.model.UserApp;
import com.example.jeuxolympiques.repository.UserAppRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserAppRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRegistrationDTO validRegistrationDTO;
    private UserApp savedUser;

    @BeforeEach
    void setUp() {
        // Configuration d'un DTO d'inscription valide pour les tests
        validRegistrationDTO = new UserRegistrationDTO();
        validRegistrationDTO.setFirstName("Jean");
        validRegistrationDTO.setLastName("Dupont");
        validRegistrationDTO.setEmail("jean.dupont@example.com");
        validRegistrationDTO.setPassword("Password1@");
        validRegistrationDTO.setConfirmPassword("Password1@");

        // Utilisateur sauvegardé simulé
        savedUser = new UserApp();
        savedUser.setUserId(1L);
        savedUser.setFirstName("Jean");
        savedUser.setLastName("Dupont");
        savedUser.setEmail("jean.dupont@example.com");
        savedUser.setPassword("encoded_password");
        savedUser.setUserKey("generated-uuid");
    }

    @Test
    void registerNewUser_WithValidData_ShouldRegisterUser() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(UserApp.class))).thenReturn(savedUser);

        // When
        UserApp registeredUser = userService.registerNewUser(validRegistrationDTO);

        // Then
        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getUserId()).isEqualTo(1L);
        assertThat(registeredUser.getEmail()).isEqualTo("jean.dupont@example.com");
        assertThat(registeredUser.getFirstName()).isEqualTo("Jean");
        assertThat(registeredUser.getLastName()).isEqualTo("Dupont");

        verify(userRepository).findByEmail("jean.dupont@example.com");
        verify(passwordEncoder).encode("Password1@");
        verify(userRepository).save(any(UserApp.class));
    }

    @Test
    void registerNewUser_WithMismatchedPasswords_ShouldThrowException() {
        // Given
        validRegistrationDTO.setConfirmPassword("DifferentPassword1@");

        // When & Then
        assertThatThrownBy(() -> userService.registerNewUser(validRegistrationDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Les mots de passe ne correspondent pas");

        verify(userRepository, never()).findByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserApp.class));
    }

    @Test
    void registerNewUser_WithExistingEmail_ShouldThrowException() {
        // Given
        UserApp existingUser = new UserApp();
        existingUser.setEmail("jean.dupont@example.com");

        when(userRepository.findByEmail("jean.dupont@example.com")).thenReturn(existingUser);

        // When & Then
        assertThatThrownBy(() -> userService.registerNewUser(validRegistrationDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cet email est déjà utilisé");

        verify(userRepository).findByEmail("jean.dupont@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserApp.class));
    }

    @Test
    void registerNewUser_ShouldGenerateUserKey() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");

        // Capture l'utilisateur sauvegardé pour vérifier le userKey
        when(userRepository.save(any(UserApp.class))).thenAnswer(invocation -> {
            UserApp savedUser = invocation.getArgument(0);
            assertThat(savedUser.getUserKey()).isNotNull().isNotEmpty();
            // Copie des valeurs sauvegardées dans notre user de test
            this.savedUser.setUserKey(savedUser.getUserKey());
            return this.savedUser;
        });

        // When
        UserApp registeredUser = userService.registerNewUser(validRegistrationDTO);

        // Then
        assertThat(registeredUser.getUserKey()).isNotNull().isNotEmpty();
        verify(userRepository).save(any(UserApp.class));
    }

    @Test
    void registerNewUser_ShouldEncodePassword() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(null);
        when(passwordEncoder.encode("Password1@")).thenReturn("encoded_password");
        when(userRepository.save(any(UserApp.class))).thenReturn(savedUser);

        // When
        userService.registerNewUser(validRegistrationDTO);

        // Then
        verify(passwordEncoder).encode("Password1@");
    }
}