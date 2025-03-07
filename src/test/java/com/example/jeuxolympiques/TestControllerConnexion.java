package com.example.jeuxolympiques;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class TestControllerConnexion {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "Public Endpoint";
    }

    @GetMapping("/profile")
    public String profileEndpoint() {
        return "Profile Endpoint (Authenticated)";
    }

    @GetMapping("/buy-ticket")
    public String buyTicketEndpoint() {
        return "Buy Ticket Endpoint (Authenticated)";
    }

    @GetMapping("/admin")
    public String adminEndpoint() {
        return "Admin Endpoint (Role ADMIN)";
    }
}
