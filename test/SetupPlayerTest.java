package test;

import network.GameServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class SetupPlayerTest { // REQ ID 1 - Between 2 and 6 players can play the game

    @Test
    public void testValidatePlayerCountWithInvalidNumber() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            GameServer.validatePlayerCount(1, 0);
        });
        assertEquals("Not enough players to start the game.", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            GameServer.validatePlayerCount(7, 0);
        });
        assertEquals("Too many players. Maximum is 6.", exception.getMessage());
    }

    @Test
    public void testValidatePlayerCountWithValidNumber() {
        assertDoesNotThrow(() -> {
            GameServer.validatePlayerCount(2, 1);
        });

        assertDoesNotThrow(() -> {
            GameServer.validatePlayerCount(6, 0);
        });
    }
}