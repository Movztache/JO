package com.example.jeuxolympiques.configuration;


import com.example.jeuxolympiques.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/home", "/offers/**").permitAll() // Pages accessibles à tous
                        .requestMatchers("/profile/**").authenticated() // Les URLs commençant par /profile/ nécessitent une authentification
                        .requestMatchers("/buy-ticket/**").authenticated() // Les URLs commençant par /buy-ticket/ nécessitent une authentification
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Les URLs commençant par /admin/ nécessitent le rôle "ADMIN".
                        .anyRequest().permitAll() // Toutes les autres requêtes sont permises sans authentification
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .defaultSuccessUrl("/home")
                        .failureUrl("/login?error")
                )
                .logout(logout -> logout
                        .permitAll()
                        .logoutSuccessUrl("/")
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
