package com.yourkoder.letsmeet.iam.authorizer.token.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InvalidAlgorithmExceptionTest {
    /**
     * Test {@link InvalidAlgorithmException#InvalidAlgorithmException(String)}.
     * <p>
     * Method under test:
     * {@link InvalidAlgorithmException#InvalidAlgorithmException(String)}
     */
    @Test
    @DisplayName("Test new InvalidAlgorithmException(String)")
    void testNewInvalidAlgorithmException() {
        // Arrange and Act
        InvalidAlgorithmException actualInvalidAlgorithmException = new InvalidAlgorithmException("An error occurred");

        // Assert
        assertEquals("An error occurred", actualInvalidAlgorithmException.getMessage());
        assertNull(actualInvalidAlgorithmException.getCause());
        assertEquals(0, actualInvalidAlgorithmException.getSuppressed().length);
    }
}
