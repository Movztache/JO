package com.example.jeuxolympiques;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class JeuxOlympiquesApplicationTests {
	@Test
	void contextLoads() {
		// Test vide pour vérifier si le contexte se charge correctement
	}
}
