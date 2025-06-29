package com.yourkoder.letsmeet.iam.authorizer.token.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtExpiredExceptionTest {
    /**
     * Test {@link JwtExpiredException#JwtExpiredException(String)}.
     * <p>
     * Method under test: {@link JwtExpiredException#JwtExpiredException(String)}
     */
    @Test
    @DisplayName("Test new JwtExpiredException(String)")
    void testNewJwtExpiredException() {
        // Arrange and Act
        JwtExpiredException actualJwtExpiredException = new JwtExpiredException("An error occurred");

        // Assert
        assertEquals("An error occurred", actualJwtExpiredException.getMessage());
        assertNull(actualJwtExpiredException.getCause());
        assertEquals(0, actualJwtExpiredException.getSuppressed().length);
    }
}
