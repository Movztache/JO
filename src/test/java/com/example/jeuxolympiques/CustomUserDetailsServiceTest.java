package com.example.jeuxolympiques;

import com.example.jeuxolympiques.model.UserApp;
import com.example.jeuxolympiques.repository.UserAppRepository;
import com.example.jeuxolympiques.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UserAppRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private UserApp testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserApp();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
    }

    @Test
    void loadUserByUsername_WithExistingUser_ShouldReturnUserDetails() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(testUser);

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("hashedPassword");

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities).extracting("authority").containsExactly("User");

        verify(userRepository).findByEmail("test@example.com");
    }


    @Test
    void loadUserByUsername_WithNonExistingUser_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nonexistent@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findByEmail("nonexistent@example.com");
    }
}