package com.yourkoder.letsmeet.iam.authorizer.token.valueobject;

import com.nimbusds.jwt.JWTClaimsSet;
import com.yourkoder.letsmeet.iam.authorizer.token.exception.JwtClaimsNotApplicableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthenticationStatusTest {

    @Test
    @DisplayName("Test withJwtClaimsSet(); given 'UNAUTHENTICATED'; then throws")
    void testWithJwtClaimsSetGivenAuthenticatedThenThrows() {
        // Arrange, Act and Assert
        assertThrows(JwtClaimsNotApplicableException.class,
                () -> AuthenticationStatus.UNAUTHENTICATED.withClaimSet(new JWTClaimsSet.Builder().build()));
    }

    /**
     * Test {@link AuthenticationStatus#getJwtClaimsSet()}.
     * <ul>
     *   <li>Then throw {@link JwtClaimsNotApplicableException}.</li>
     * </ul>
     * <p>
     * Method under test: {@link AuthenticationStatus#getJwtClaimsSet()}
     */
    @Test
    @DisplayName("Test getJwtClaimsSet(); then throw JwtClaimsNotApplicableException")
    void testGetJwtClaimsSetThenThrowJwtClaimsNotApplicableException() {
        // Arrange, Act and Assert
        assertThrows(JwtClaimsNotApplicableException.class,
                () -> AuthenticationStatus.UNAUTHENTICATED.getJwtClaimsSet());
    }
}
