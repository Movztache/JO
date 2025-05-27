package com.example.vibetickets;

import com.example.vibetickets.service.impl.LogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LogServiceImplTest {

    private LogServiceImpl logService;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern LOG_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} \\[\\w+] .*");

    @BeforeEach
    void setUp() {
        logService = new LogServiceImpl();
    }

    @Test
    void info_shouldAddLogToList() {
        // Arrange
        String message = "Test info message";

        // Act
        logService.info(message);
        List<String> logs = logService.getRecentLogs();

        // Assert
        assertEquals(1, logs.size());
        assertTrue(logs.getFirst().contains("[INFO] " + message));
        assertTrue(LOG_PATTERN.matcher(logs.getFirst()).matches(), "Le format du log ne correspond pas au pattern attendu");
    }

    @Test
    void warning_shouldAddLogToList() {
        // Arrange
        String message = "Test warning message";

        // Act
        logService.warning(message);
        List<String> logs = logService.getRecentLogs();

        // Assert
        assertEquals(1, logs.size());
        assertTrue(logs.getFirst().contains("[WARN] " + message));
        assertTrue(LOG_PATTERN.matcher(logs.getFirst()).matches());
    }

    @Test
    void error_shouldAddLogToListWithException() {
        // Arrange
        String message = "Test error message";
        Exception exception = new RuntimeException("Test exception");

        // Act
        logService.error(message, exception);
        List<String> logs = logService.getRecentLogs();

        // Assert
        assertEquals(1, logs.size());
        assertTrue(logs.getFirst().contains("[ERROR] " + message));
        assertTrue(logs.getFirst().contains("Test exception"));
        assertTrue(LOG_PATTERN.matcher(logs.getFirst()).matches());
    }

    @Test
    void error_shouldAddLogToListWithoutException() {
        // Arrange
        String message = "Test error message";

        // Act
        logService.error(message, null);
        List<String> logs = logService.getRecentLogs();

        // Assert
        assertEquals(1, logs.size());
        assertTrue(logs.getFirst().contains("[ERROR] " + message));
        assertTrue(LOG_PATTERN.matcher(logs.getFirst()).matches());
    }

    @Test
    void userAction_shouldAddLogToList() {
        // Arrange
        Long userId = 123L;
        String action = "création de réservation";

        // Act
        logService.userAction(userId, action);
        List<String> logs = logService.getRecentLogs();

        // Assert
        assertEquals(1, logs.size());
        assertTrue(logs.getFirst().contains("[ACTION] Utilisateur " + userId + " a effectué: " + action));
        assertTrue(LOG_PATTERN.matcher(logs.getFirst()).matches());
    }

    @Test
    void getRecentLogs_shouldReturnUnmodifiableList() {
        // Arrange
        logService.info("Test message");

        // Act
        List<String> logs = logService.getRecentLogs();

        // Assert
        assertThrows(UnsupportedOperationException.class, () -> logs.add("Nouveau log non autorisé"));
    }

    @Test
    void addToRecentLogs_shouldLimitTheNumberOfLogs() {
        // Arrange - Le MAX_LOGS est défini à 50
        for (int i = 0; i < 60; i++) {
            logService.info("Test message " + i);
        }

        // Act
        List<String> logs = logService.getRecentLogs();

        // Assert
        assertEquals(50, logs.size(), "La liste devrait être limitée à 50 éléments");
        assertTrue(logs.getFirst().contains("Test message 59"), "Le log le plus récent devrait être en premier");
        assertTrue(logs.get(49).contains("Test message 10"), "Le log le plus ancien devrait être en dernier");
    }

    @Test
    void addToRecentLogs_shouldAddNewestLogsAtTheBeginning() {
        // Arrange
        logService.info("Premier message");
        logService.info("Deuxième message");
        logService.info("Troisième message");

        // Act
        List<String> logs = logService.getRecentLogs();

        // Assert
        assertEquals(3, logs.size());
        assertTrue(logs.get(0).contains("Troisième message"), "Le log le plus récent devrait être en premier");
        assertTrue(logs.get(1).contains("Deuxième message"), "Le second log devrait être au milieu");
        assertTrue(logs.get(2).contains("Premier message"), "Le log le plus ancien devrait être en dernier");
    }

    @Test
    void addToRecentLogs_shouldIncludeTimestampAndLevel() {
        // Arrange
        String currentDatePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Act
        logService.info("Test message");
        List<String> logs = logService.getRecentLogs();
        String log = logs.getFirst();

        // Assert
        assertTrue(log.startsWith(currentDatePrefix), "Le log devrait commencer par la date du jour");
        assertTrue(log.contains("[INFO]"), "Le log devrait contenir le niveau INFO");
    }
}