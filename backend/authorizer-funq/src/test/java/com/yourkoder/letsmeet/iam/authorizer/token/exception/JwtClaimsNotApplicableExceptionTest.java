package com.yourkoder.letsmeet.iam.authorizer.token.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtClaimsNotApplicableExceptionTest {
    /**
     * Test
     * {@link JwtClaimsNotApplicableException#JwtClaimsNotApplicableException(String)}.
     * <p>
     * Method under test:
     * {@link JwtClaimsNotApplicableException#JwtClaimsNotApplicableException(String)}
     */
    @Test
    @DisplayName("Test new JwtClaimsNotApplicableException(String)")
    void testNewJwtClaimsNotApplicableException() {
        // Arrange and Act
        JwtClaimsNotApplicableException actualJwtClaimsNotApplicableException = new JwtClaimsNotApplicableException(
                "An error occurred");

        // Assert
        assertEquals("An error occurred", actualJwtClaimsNotApplicableException.getMessage());
        assertNull(actualJwtClaimsNotApplicableException.getCause());
        assertEquals(0, actualJwtClaimsNotApplicableException.getSuppressed().length);
    }
}
