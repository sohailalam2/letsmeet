package com.yourkoder.letsmeet.iam.authorizer.token.valueobject.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InvalidJwtFormatExceptionTest {
    /**
     * Test {@link InvalidJwtFormatException#InvalidJwtFormatException(String)}.
     * <p>
     * Method under test:
     * {@link InvalidJwtFormatException#InvalidJwtFormatException(String)}
     */
    @Test
    @DisplayName("Test new InvalidJwtFormatException(String)")
    void testNewInvalidJwtFormatException() {
        // Arrange and Act
        InvalidJwtFormatException actualInvalidJwtFormatException = new InvalidJwtFormatException("An error occurred");

        // Assert
        assertEquals("An error occurred", actualInvalidJwtFormatException.getMessage());
        assertNull(actualInvalidJwtFormatException.getCause());
        assertEquals(0, actualInvalidJwtFormatException.getSuppressed().length);
    }
}
