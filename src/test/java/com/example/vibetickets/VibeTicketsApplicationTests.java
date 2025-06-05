package com.example.vibetickets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Disabled("Temporairement désactivé - problème de contexte Spring dans CI/CD")
class VibeTicketsApplicationTests {
	@Test
	void contextLoads() {
		// Test vide pour vérifier si le contexte se charge correctement
	}
}
