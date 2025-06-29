package com.yourkoder.letsmeet.iam.authorizer.token.valueobject;

import com.yourkoder.letsmeet.iam.authorizer.token.valueobject.exception.InvalidJwtFormatException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
class JWTTypeTest {

    private static final String JWS_TOKEN = "header.payload.signature";
    private static final String JWE_TOKEN = "header.payload.ciphertext.iv.tag";
    private static final String NONE_TOKEN = "header";
    private static final String INVALID_TOKEN = "header.payload";

    @Test
    void getTokenTypeWithJWSComponentsReturnsJWS() throws InvalidJwtFormatException {
        JWTType result = JWTType.getTokenType(JWS_TOKEN);
        assertEquals(JWTType.JWS, result);
        assertEquals(3, result.getComponents());
    }

    @Test
    void getTokenTypeWithJWEComponentsReturnsJWE() throws InvalidJwtFormatException {
        JWTType result = JWTType.getTokenType(JWE_TOKEN);
        assertEquals(JWTType.JWE, result);
        assertEquals(5, result.getComponents());
    }

    @Test
    void getTokenTypeWithNoneComponentsReturnsNONE() throws InvalidJwtFormatException {
        JWTType result = JWTType.getTokenType(NONE_TOKEN);
        assertEquals(JWTType.NONE, result);
        assertEquals(1, result.getComponents());
    }

    @Test
    void getTokenTypeWithInvalidComponentsThrowsInvalidJwtFormatException() {
        Exception exception = assertThrows(InvalidJwtFormatException.class, () -> {
            JWTType.getTokenType(INVALID_TOKEN);
        });
        assertTrue(exception.getMessage().contains("Invalid JWT"));
        assertTrue(exception.getMessage().contains("Can not get token type"));
    }

    @Test
    void getTokenComponentsWithJWSComponentsReturnsThree() {
        int components = JWTType.getTokenComponents(JWS_TOKEN);
        assertEquals(3, components);
    }

    @Test
    void getTokenComponentsWithJWEComponentsReturnsFive() {
        int components = JWTType.getTokenComponents(JWE_TOKEN);
        assertEquals(5, components);
    }

    @Test
    void getTokenComponentsWithNoneComponentsReturnsOne() {
        int components = JWTType.getTokenComponents(NONE_TOKEN);
        assertEquals(1, components);
    }

    @Test
    void getTokenComponentsWithInvalidComponentsReturnsTwo() {
        int components = JWTType.getTokenComponents(INVALID_TOKEN);
        assertEquals(2, components);
    }
}