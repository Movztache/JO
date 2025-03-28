package com.example.jeuxolympiques;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.jeuxolympiques.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.core.userdetails.UserDetailsService;
import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;

@SpringBootTest
@AutoConfigureMockMvc
@Import({SecurityConfig.class, SecurityConfigTest.SecurityTestConfig.class})
@ActiveProfiles("test")
public class SecurityConfigTest {

    @TestConfiguration
    static class SecurityTestConfig {

        @Bean
        @Primary
        public UserDetailsService userDetailsService() {
            // Créez un utilisateur avec le rôle ADMIN pour les tests
            return username -> {
                if (username.equals("admin@test.com")) {
                    return new org.springframework.security.core.userdetails.User(
                            "admin@test.com",
                            "password",
                            Collections.singletonList(new SimpleGrantedAuthority("Admin"))
                    );
                } else {
                    return new org.springframework.security.core.userdetails.User(
                            username,
                            "password",
                            Collections.singletonList(new SimpleGrantedAuthority("User"))
                    );
                }
            };
        }

    }


    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }


    @Test
    public void testLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    public void testHomePage() throws Exception {
        // Adaptation pour refléter le comportement actuel
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void testAdminPageWithoutAuth() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?unauthorized"));  // Vérifier l'URL exacte de redirection
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAdminPageWithAuth() throws Exception {

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk());
    }

    @Test
    public void testLogin() throws Exception {
        mockMvc.perform(formLogin().user("user@example.com").password("password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }
}
