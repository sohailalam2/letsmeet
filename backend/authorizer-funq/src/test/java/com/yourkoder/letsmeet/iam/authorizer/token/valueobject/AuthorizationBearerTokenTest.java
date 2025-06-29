package com.yourkoder.letsmeet.iam.authorizer.token.valueobject;

import com.yourkoder.letsmeet.iam.authorizer.token.valueobject.exception.InvalidJwtFormatException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
class AuthorizationBearerTokenTest {

    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.signature";
    private static final String VALID_AUTH_HEADER = "Bearer " + VALID_TOKEN;

    @Test
    void constructorWithValidHeaderSetsValueAndJwtType() throws InvalidJwtFormatException {
        AuthorizationBearerToken token = new AuthorizationBearerToken(VALID_AUTH_HEADER);
        assertEquals(VALID_TOKEN, token.getValue());
        assertNotNull(token.getJwtType());
    }

    @Test
    void constructorWithNullHeaderThrowsInvalidJwtFormatException() {
        Exception exception = assertThrows(InvalidJwtFormatException.class, () -> {
            new AuthorizationBearerToken(null);
        });
        assertTrue(exception.getMessage().contains("The authorisation token provided is null"));
    }

    @Test
    void constructorWithInvalidFormatThrowsInvalidJwtFormatException() {
        String invalidHeader = "InvalidToken";
        Exception exception = assertThrows(InvalidJwtFormatException.class, () -> {
            new AuthorizationBearerToken(invalidHeader);
        });
        assertTrue(exception.getMessage().contains("does not have a valid format"));
    }

    @Test
    void constructorWithNonBearerPrefixThrowsInvalidJwtFormatException() {
        String invalidHeader = "Basic " + VALID_TOKEN;
        Exception exception = assertThrows(InvalidJwtFormatException.class, () -> {
            new AuthorizationBearerToken(invalidHeader);
        });
        assertTrue(exception.getMessage().contains("is not a Bearer token"));
    }

    @Test
    void fromTokenStringWithValidTokenCreatesValidToken() throws InvalidJwtFormatException {
        AuthorizationBearerToken token = AuthorizationBearerToken.fromTokenString(VALID_TOKEN);
        assertEquals(VALID_TOKEN, token.getValue());
        assertNotNull(token.getJwtType());
    }

    @Test
    void toStringReturnsValue() throws InvalidJwtFormatException {
        AuthorizationBearerToken token = new AuthorizationBearerToken(VALID_AUTH_HEADER);
        assertEquals(VALID_TOKEN, token.toString());
    }
}