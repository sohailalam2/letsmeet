package com.yourkoder.letsmeet.iam.authorizer.policy.dtos.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AuthorizationPolicyResponseTest {
    /**
     * Test {@link AuthorizationPolicyResponse#equals(Object)}, and
     * {@link AuthorizationPolicyResponse#hashCode()}.
     * <ul>
     *   <li>When other is equal.</li>
     *   <li>Then return equal.</li>
     * </ul>
     * <p>
     * Methods under test:
     * <ul>
     *   <li>{@link AuthorizationPolicyResponse#equals(Object)}
     *   <li>{@link AuthorizationPolicyResponse#hashCode()}
     * </ul>
     */
    @Test
    @DisplayName("Test equals(Object), and hashCode(); when other is equal; then return equal")
    void testEqualsAndHashCodeWhenOtherIsEqualThenReturnEqual() {
        // Arrange
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder builderResult = AuthorizationPolicyResponse
                .builder();
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder contextResult = builderResult
                .context(new HashMap<>());
        AuthorizationPolicyResponse buildResult = contextResult.policyDocument(new HashMap<>()).principalId("42")
                .build();
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder builderResult2 = AuthorizationPolicyResponse
                .builder();
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder contextResult2 = builderResult2
                .context(new HashMap<>());
        AuthorizationPolicyResponse buildResult2 = contextResult2.policyDocument(new HashMap<>()).principalId("42")
                .build();

        // Act and Assert
        assertEquals(buildResult, buildResult2);
        int expectedHashCodeResult = buildResult.hashCode();
        assertEquals(expectedHashCodeResult, buildResult2.hashCode());
    }

    /**
     * Test {@link AuthorizationPolicyResponse#equals(Object)}, and
     * {@link AuthorizationPolicyResponse#hashCode()}.
     * <ul>
     *   <li>When other is same.</li>
     *   <li>Then return equal.</li>
     * </ul>
     * <p>
     * Methods under test:
     * <ul>
     *   <li>{@link AuthorizationPolicyResponse#equals(Object)}
     *   <li>{@link AuthorizationPolicyResponse#hashCode()}
     * </ul>
     */
    @Test
    @DisplayName("Test equals(Object), and hashCode(); when other is same; then return equal")
    void testEqualsAndHashCodeWhenOtherIsSameThenReturnEqual() {
        // Arrange
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder builderResult = AuthorizationPolicyResponse
                .builder();
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder contextResult = builderResult
                .context(new HashMap<>());
        AuthorizationPolicyResponse buildResult = contextResult.policyDocument(new HashMap<>()).principalId("42")
                .build();

        // Act and Assert
        assertEquals(buildResult, buildResult);
        int expectedHashCodeResult = buildResult.hashCode();
        assertEquals(expectedHashCodeResult, buildResult.hashCode());
    }

    /**
     * Test {@link AuthorizationPolicyResponse#equals(Object)}.
     * <ul>
     *   <li>When other is different.</li>
     *   <li>Then return not equal.</li>
     * </ul>
     * <p>
     * Method under test: {@link AuthorizationPolicyResponse#equals(Object)}
     */
    @Test
    @DisplayName("Test equals(Object); when other is different; then return not equal")
    void testEqualsWhenOtherIsDifferentThenReturnNotEqual() {
        // Arrange
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder authorizationPolicyResponseBuilder = mock(
                AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder.class);
        when(authorizationPolicyResponseBuilder.context(Mockito.<Map<String, String>>any()))
                .thenReturn(AuthorizationPolicyResponse.builder());
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder contextResult
                = authorizationPolicyResponseBuilder
                .context(new HashMap<>());
        AuthorizationPolicyResponse buildResult = contextResult.policyDocument(new HashMap<>()).principalId("42")
                .build();
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder builderResult = AuthorizationPolicyResponse
                .builder();
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder contextResult2 = builderResult
                .context(new HashMap<>());
        AuthorizationPolicyResponse buildResult2 = contextResult2.policyDocument(new HashMap<>()).principalId("42")
                .build();

        // Act and Assert
        assertNotEquals(buildResult, buildResult2);
    }

    /**
     * Test {@link AuthorizationPolicyResponse#equals(Object)}.
     * <ul>
     *   <li>When other is different.</li>
     *   <li>Then return not equal.</li>
     * </ul>
     * <p>
     * Method under test: {@link AuthorizationPolicyResponse#equals(Object)}
     */
    @Test
    @DisplayName("Test equals(Object); when other is different; then return not equal")
    void testEqualsWhenOtherIsDifferentThenReturnNotEqual2() {
        // Arrange
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder authorizationPolicyResponseBuilder = mock(
                AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder.class);
        when(authorizationPolicyResponseBuilder.policyDocument(Mockito.<Map<String, Object>>any()))
                .thenReturn(AuthorizationPolicyResponse.builder());
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder authorizationPolicyResponseBuilder2 = mock(
                AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder.class);
        when(authorizationPolicyResponseBuilder2.context(Mockito.<Map<String, String>>any()))
                .thenReturn(authorizationPolicyResponseBuilder);
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder contextResult
                = authorizationPolicyResponseBuilder2
                .context(new HashMap<>());
        AuthorizationPolicyResponse buildResult = contextResult.policyDocument(new HashMap<>()).principalId("42")
                .build();
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder builderResult = AuthorizationPolicyResponse
                .builder();
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder contextResult2 = builderResult
                .context(new HashMap<>());
        AuthorizationPolicyResponse buildResult2 = contextResult2.policyDocument(new HashMap<>()).principalId("42")
                .build();

        // Act and Assert
        assertNotEquals(buildResult, buildResult2);
    }

    /**
     * Test {@link AuthorizationPolicyResponse#equals(Object)}.
     * <ul>
     *   <li>When other is different.</li>
     *   <li>Then return not equal.</li>
     * </ul>
     * <p>
     * Method under test: {@link AuthorizationPolicyResponse#equals(Object)}
     */
    @Test
    @DisplayName("Test equals(Object); when other is different; then return not equal")
    void testEqualsWhenOtherIsDifferentThenReturnNotEqual3() {
        // Arrange
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder authorizationPolicyResponseBuilder = mock(
                AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder.class);
        when(authorizationPolicyResponseBuilder.principalId(Mockito.<String>any()))
                .thenReturn(AuthorizationPolicyResponse.builder());
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder authorizationPolicyResponseBuilder2 = mock(
                AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder.class);
        when(authorizationPolicyResponseBuilder2.policyDocument(Mockito.<Map<String, Object>>any()))
                .thenReturn(authorizationPolicyResponseBuilder);
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder authorizationPolicyResponseBuilder3 = mock(
                AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder.class);
        when(authorizationPolicyResponseBuilder3.context(Mockito.<Map<String, String>>any()))
                .thenReturn(authorizationPolicyResponseBuilder2);
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder contextResult
                = authorizationPolicyResponseBuilder3
                .context(new HashMap<>());
        AuthorizationPolicyResponse buildResult = contextResult.policyDocument(new HashMap<>()).principalId("42")
                .build();
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder builderResult = AuthorizationPolicyResponse
                .builder();
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder contextResult2 = builderResult
                .context(new HashMap<>());
        AuthorizationPolicyResponse buildResult2 = contextResult2.policyDocument(new HashMap<>()).principalId("42")
                .build();

        // Act and Assert
        assertNotEquals(buildResult, buildResult2);
    }

    /**
     * Test {@link AuthorizationPolicyResponse#equals(Object)}.
     * <ul>
     *   <li>When other is different.</li>
     *   <li>Then return not equal.</li>
     * </ul>
     * <p>
     * Method under test: {@link AuthorizationPolicyResponse#equals(Object)}
     */
    @Test
    @DisplayName("Test equals(Object); when other is different; then return not equal")
    void testEqualsWhenOtherIsDifferentThenReturnNotEqual4() {
        // Arrange
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder authorizationPolicyResponseBuilder = mock(
                AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder.class);
        when(authorizationPolicyResponseBuilder.principalId(Mockito.<String>any()))
                .thenReturn(AuthorizationPolicyResponse.builder());
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder authorizationPolicyResponseBuilder2 = mock(
                AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder.class);
        when(authorizationPolicyResponseBuilder2.policyDocument(Mockito.<Map<String, Object>>any()))
                .thenReturn(authorizationPolicyResponseBuilder);
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder authorizationPolicyResponseBuilder3 = mock(
                AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder.class);
        when(authorizationPolicyResponseBuilder3.context(Mockito.<Map<String, String>>any()))
                .thenReturn(authorizationPolicyResponseBuilder2);
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder contextResult
                = authorizationPolicyResponseBuilder3
                .context(new HashMap<>());
        AuthorizationPolicyResponse buildResult = contextResult.policyDocument(new HashMap<>()).principalId("42")
                .build();
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder builderResult = AuthorizationPolicyResponse
                .builder();
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder contextResult2 = builderResult
                .context(new HashMap<>());
        AuthorizationPolicyResponse buildResult2 = contextResult2.policyDocument(new HashMap<>()).principalId(null)
                .build();

        // Act and Assert
        assertNotEquals(buildResult, buildResult2);
    }

    /**
     * Test {@link AuthorizationPolicyResponse#equals(Object)}.
     * <ul>
     *   <li>When other is different.</li>
     *   <li>Then return not equal.</li>
     * </ul>
     * <p>
     * Method under test: {@link AuthorizationPolicyResponse#equals(Object)}
     */
    @Test
    @DisplayName("Test equals(Object); when other is different; then return not equal")
    void testEqualsWhenOtherIsDifferentThenReturnNotEqual5() {
        // Arrange
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder authorizationPolicyResponseBuilder = mock(
                AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder.class);
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder builderResult = AuthorizationPolicyResponse
                .builder();
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder contextResult = builderResult
                .context(new HashMap<>());
        AuthorizationPolicyResponse buildResult = contextResult.policyDocument(new HashMap<>()).principalId("42")
                .build();
        when(authorizationPolicyResponseBuilder.build()).thenReturn(buildResult);
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder authorizationPolicyResponseBuilder2 = mock(
                AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder.class);
        when(authorizationPolicyResponseBuilder2.principalId(Mockito.<String>any()))
                .thenReturn(authorizationPolicyResponseBuilder);
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder authorizationPolicyResponseBuilder3 = mock(
                AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder.class);
        when(authorizationPolicyResponseBuilder3.policyDocument(Mockito.<Map<String, Object>>any()))
                .thenReturn(authorizationPolicyResponseBuilder2);
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder authorizationPolicyResponseBuilder4 = mock(
                AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder.class);
        when(authorizationPolicyResponseBuilder4.context(Mockito.<Map<String, String>>any()))
                .thenReturn(authorizationPolicyResponseBuilder3);
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder contextResult2
                = authorizationPolicyResponseBuilder4
                .context(new HashMap<>());
        AuthorizationPolicyResponse buildResult2 = contextResult2.policyDocument(new HashMap<>()).principalId("42")
                .build();
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder builderResult2 = AuthorizationPolicyResponse
                .builder();
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder contextResult3 = builderResult2
                .context(new HashMap<>());
        AuthorizationPolicyResponse buildResult3 = contextResult3.policyDocument(new HashMap<>()).principalId(null)
                .build();

        // Act and Assert
        assertNotEquals(buildResult2, buildResult3);
    }

    /**
     * Test {@link AuthorizationPolicyResponse#equals(Object)}.
     * <ul>
     *   <li>When other is {@code null}.</li>
     *   <li>Then return not equal.</li>
     * </ul>
     * <p>
     * Method under test: {@link AuthorizationPolicyResponse#equals(Object)}
     */
    @Test
    @DisplayName("Test equals(Object); when other is 'null'; then return not equal")
    void testEqualsWhenOtherIsNullThenReturnNotEqual() {
        // Arrange
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder builderResult = AuthorizationPolicyResponse
                .builder();
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder contextResult = builderResult
                .context(new HashMap<>());
        AuthorizationPolicyResponse buildResult = contextResult.policyDocument(new HashMap<>()).principalId("42")
                .build();

        // Act and Assert
        assertNotEquals(buildResult, null);
    }

    /**
     * Test {@link AuthorizationPolicyResponse#equals(Object)}.
     * <ul>
     *   <li>When other is wrong type.</li>
     *   <li>Then return not equal.</li>
     * </ul>
     * <p>
     * Method under test: {@link AuthorizationPolicyResponse#equals(Object)}
     */
    @Test
    @DisplayName("Test equals(Object); when other is wrong type; then return not equal")
    void testEqualsWhenOtherIsWrongTypeThenReturnNotEqual() {
        // Arrange
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder builderResult = AuthorizationPolicyResponse
                .builder();
        AuthorizationPolicyResponse.AuthorizationPolicyResponseBuilder contextResult = builderResult
                .context(new HashMap<>());
        AuthorizationPolicyResponse buildResult = contextResult.policyDocument(new HashMap<>()).principalId("42")
                .build();

        // Act and Assert
        assertNotEquals(buildResult, "Different type to AuthorizationPolicyResponse");
    }

    /**
     * Test getters and setters.
     * <p>
     * Methods under test:
     * <ul>
     *   <li>{@link AuthorizationPolicyResponse#AuthorizationPolicyResponse()}
     *   <li>{@link AuthorizationPolicyResponse#setContext(Map)}
     *   <li>{@link AuthorizationPolicyResponse#setPolicyDocument(Map)}
     *   <li>{@link AuthorizationPolicyResponse#setPrincipalId(String)}
     *   <li>{@link AuthorizationPolicyResponse#toString()}
     *   <li>{@link AuthorizationPolicyResponse#getContext()}
     *   <li>{@link AuthorizationPolicyResponse#getPolicyDocument()}
     *   <li>{@link AuthorizationPolicyResponse#getPrincipalId()}
     * </ul>
     */
    @Test
    @DisplayName("Test getters and setters")
    void testGettersAndSetters() {
        // Arrange and Act
        AuthorizationPolicyResponse actualAuthorizationPolicyResponse = new AuthorizationPolicyResponse();
        HashMap<String, String> context = new HashMap<>();
        actualAuthorizationPolicyResponse.setContext(context);
        HashMap<String, Object> policyDocument = new HashMap<>();
        actualAuthorizationPolicyResponse.setPolicyDocument(policyDocument);
        actualAuthorizationPolicyResponse.setPrincipalId("42");
        String actualToStringResult = actualAuthorizationPolicyResponse.toString();
        Map<String, String> actualContext = actualAuthorizationPolicyResponse.getContext();
        Map<String, Object> actualPolicyDocument = actualAuthorizationPolicyResponse.getPolicyDocument();

        // Assert that nothing has changed
        assertEquals("42", actualAuthorizationPolicyResponse.getPrincipalId());
        assertEquals(
                "AuthorizationPolicyResponse(principalId=42, policyDocument={}, context={})",
                actualToStringResult
        );
        assertTrue(actualContext.isEmpty());
        assertTrue(actualPolicyDocument.isEmpty());
        assertSame(context, actualContext);
        assertSame(policyDocument, actualPolicyDocument);
    }

    /**
     * Test getters and setters.
     * <ul>
     *   <li>When {@code 42}.</li>
     * </ul>
     * <p>
     * Methods under test:
     * <ul>
     *   <li>
     * {@link AuthorizationPolicyResponse#AuthorizationPolicyResponse(String, Map, Map)}
     *   <li>{@link AuthorizationPolicyResponse#setContext(Map)}
     *   <li>{@link AuthorizationPolicyResponse#setPolicyDocument(Map)}
     *   <li>{@link AuthorizationPolicyResponse#setPrincipalId(String)}
     *   <li>{@link AuthorizationPolicyResponse#toString()}
     *   <li>{@link AuthorizationPolicyResponse#getContext()}
     *   <li>{@link AuthorizationPolicyResponse#getPolicyDocument()}
     *   <li>{@link AuthorizationPolicyResponse#getPrincipalId()}
     * </ul>
     */
    @Test
    @DisplayName("Test getters and setters; when '42'")
    void testGettersAndSettersWhen42() {
        // Arrange
        HashMap<String, Object> policyDocument = new HashMap<>();

        // Act
        AuthorizationPolicyResponse actualAuthorizationPolicyResponse = new AuthorizationPolicyResponse("42",
                policyDocument, new HashMap<>());
        HashMap<String, String> context = new HashMap<>();
        actualAuthorizationPolicyResponse.setContext(context);
        HashMap<String, Object> policyDocument2 = new HashMap<>();
        actualAuthorizationPolicyResponse.setPolicyDocument(policyDocument2);
        actualAuthorizationPolicyResponse.setPrincipalId("42");
        String actualToStringResult = actualAuthorizationPolicyResponse.toString();
        Map<String, String> actualContext = actualAuthorizationPolicyResponse.getContext();
        Map<String, Object> actualPolicyDocument = actualAuthorizationPolicyResponse.getPolicyDocument();

        // Assert that nothing has changed
        assertEquals("42", actualAuthorizationPolicyResponse.getPrincipalId());
        assertEquals(
                "AuthorizationPolicyResponse(principalId=42, policyDocument={}, context={})",
                actualToStringResult
        );
        assertTrue(actualContext.isEmpty());
        assertTrue(actualPolicyDocument.isEmpty());
        assertSame(context, actualContext);
        assertSame(policyDocument2, actualPolicyDocument);
    }
}
