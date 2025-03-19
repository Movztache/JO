package com.example.jeuxolympiques;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.jeuxolympiques.dto.UserRegistrationDTO;
import com.example.jeuxolympiques.model.UserApp;
import com.example.jeuxolympiques.repository.UserAppRepository;
import com.example.jeuxolympiques.service.UserAppServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserAppServiceTest {

    @Mock
    private UserAppRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserAppServiceImpl userService;

    private UserRegistrationDTO registrationDTO;
    private UserApp mockUser;

    @BeforeEach
    void setUp() {
        // Configuration de DTO d'inscription
        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setFirstName("Jean");
        registrationDTO.setLastName("Dupont");
        registrationDTO.setEmail("jean.dupont@example.com");
        registrationDTO.setPassword("Password1!");
        registrationDTO.setConfirmPassword("Password1!");

        // Configuration d'un utilisateur fictif
        mockUser = new UserApp();
        mockUser.setUserId(1L);
        mockUser.setFirstName("Jean");
        mockUser.setLastName("Dupont");
        mockUser.setEmail("jean.dupont@example.com");
        mockUser.setPassword("encodedPassword");
        mockUser.setUserKey("user-key-123");
    }

    @Test
    void testRegisterNewUser_Success() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserApp.class))).thenReturn(mockUser);

        // Act
        UserApp result = userService.registerNewUser(registrationDTO);

        // Assert
        assertNotNull(result);
        assertEquals(mockUser.getFirstName(), result.getFirstName());
        assertEquals(mockUser.getLastName(), result.getLastName());
        assertEquals(mockUser.getEmail(), result.getEmail());

        verify(userRepository).existsByEmail("jean.dupont@example.com");
        verify(passwordEncoder).encode("Password1!");
        verify(userRepository).save(any(UserApp.class));
    }

    @Test
    void testRegisterNewUser_UserAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail("jean.dupont@example.com")).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerNewUser(registrationDTO);
        });

        assertEquals("Email déjà utilisé", exception.getMessage());
        verify(userRepository).existsByEmail("jean.dupont@example.com");
        verify(userRepository, never()).save(any(UserApp.class));
    }

    @Test
    void testRegisterNewUser_PasswordMismatch() {
        // Arrange
        registrationDTO.setConfirmPassword("DifferentPassword1!");

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerNewUser(registrationDTO);
        });

        assertEquals("Les mots de passe ne correspondent pas", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(UserApp.class));
    }

    @Test
    void testFindByUserKey_Success() {
        // Arrange
        when(userRepository.findByUserKey("user-key-123")).thenReturn(Optional.of(mockUser));

        // Act
        UserApp result = userService.findByUserKey("user-key-123");

        // Assert
        assertNotNull(result);
        assertEquals(mockUser.getUserId(), result.getUserId());
        assertEquals(mockUser.getUserKey(), result.getUserKey());

        verify(userRepository).findByUserKey("user-key-123");
    }

    @Test
    void testFindByUserKey_NotFound() {
        // Arrange
        when(userRepository.findByUserKey("non-existent-key")).thenReturn(Optional.empty());

        // Act
        UserApp result = userService.findByUserKey("non-existent-key");

        // Assert
        assertNull(result);
        verify(userRepository).findByUserKey("non-existent-key");
    }

    @Test
    void testGenerateUniqueUserKey() {
        // Arrange
        when(userRepository.existsByUserKey(anyString())).thenReturn(false);

        // Act
        String result = userService.generateUniqueUserKey();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(userRepository).existsByUserKey(result);
    }

    @Test
    void testGenerateUniqueUserKey_MultipleAttempts() {
        // Arrange - La première clé existe déjà, la deuxième est unique
        when(userRepository.existsByUserKey(anyString()))
                .thenReturn(true)  // Premier appel - la clé existe déjà
                .thenReturn(false); // Deuxième appel - la clé est unique

        // Act
        String result = userService.generateUniqueUserKey();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(userRepository, times(2)).existsByUserKey(anyString());
    }

    @Test
    void testRenewUserKey_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.existsByUserKey(anyString())).thenReturn(false);
        when(userRepository.save(any(UserApp.class))).thenReturn(mockUser);

        // Act
        UserApp result = userService.renewUserKey(1L);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).save(mockUser);
    }

    @Test
    void testRenewUserKey_UserNotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.renewUserKey(99L);
        });

        assertEquals("Utilisateur non trouvé avec l'ID : 99", exception.getMessage());
        verify(userRepository).findById(99L);
        verify(userRepository, never()).save(any(UserApp.class));
    }

    @Test
    void testValidateUserKey_ValidKey() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // Act
        boolean result = userService.validateUserKey(1L, "user-key-123");

        // Assert
        assertTrue(result);
        verify(userRepository).findById(1L);
    }

    @Test
    void testValidateUserKey_InvalidKey() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // Act
        boolean result = userService.validateUserKey(1L, "wrong-key");

        // Assert
        assertFalse(result);
        verify(userRepository).findById(1L);
    }

    @Test
    void testValidateUserKey_UserNotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        boolean result = userService.validateUserKey(99L, "any-key");

        // Assert
        assertFalse(result);
        verify(userRepository).findById(99L);
    }
}