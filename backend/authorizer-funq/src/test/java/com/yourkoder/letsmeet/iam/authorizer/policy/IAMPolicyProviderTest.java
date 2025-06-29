package com.yourkoder.letsmeet.iam.authorizer.policy;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.yourkoder.letsmeet.iam.authorizer.policy.dtos.response.AuthorizationPolicyResponse;
import com.yourkoder.letsmeet.iam.authorizer.policy.exception.PolicyGenerationFailedException;
import com.yourkoder.letsmeet.iam.authorizer.token.exception.JwtClaimsNotApplicableException;
import com.yourkoder.letsmeet.iam.authorizer.token.valueobject.AuthenticationStatus;
import com.yourkoder.letsmeet.iam.config.ApplicationConfig;
import com.yourkoder.letsmeet.iam.config.AuthorizerConfig;
import com.yourkoder.letsmeet.iam.config.PolicyConfig;

import java.text.ParseException;
import java.util.HashMap;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IAMPolicyProviderTest {

    /**
     * Test
     * {@link IAMPolicyProvider#getAuthorizationPolicyDocument(AuthenticationStatus, String)}.
     * <ul>
     *   <li>Then return PolicyDocument size is two.</li>
     * </ul>
     * <p>
     * Method under test:
     * {@link IAMPolicyProvider#getAuthorizationPolicyDocument(AuthenticationStatus, String)}
     */
    @Test
    @DisplayName("Test getUnauthorizedPolicyDocument(AuthenticationStatus, String); then PolicyDocument size is two")
    void testGetUnuthorizationPolicyDocumentThenReturnPolicyDocumentSizeIsTwo()
            throws PolicyGenerationFailedException {
        // Arrange
        PolicyConfig policyConfig = mock(PolicyConfig.class);
        when(policyConfig.unauthorizedPrincipal()).thenReturn("JaneDoe");
        AuthorizerConfig authorizerConfig = mock(AuthorizerConfig.class);
        when(authorizerConfig.policy()).thenReturn(policyConfig);
        ApplicationConfig applicationConfig = mock(ApplicationConfig.class);
        when(applicationConfig.authorizer()).thenReturn(authorizerConfig);

        // Act
        AuthorizationPolicyResponse actualAuthorizationPolicyDocument = (new IAMPolicyProvider(applicationConfig))
                .getAuthorizationPolicyDocument(AuthenticationStatus.UNAUTHENTICATED, "Method Arn");

        // Assert
        verify(applicationConfig).authorizer();
        verify(authorizerConfig).policy();
        verify(policyConfig).unauthorizedPrincipal();
        Map<String, Object> policyDocument = actualAuthorizationPolicyDocument.getPolicyDocument();
        assertEquals(2, policyDocument.size());
        Object getResult = policyDocument.get("Statement");
        Map<Object, Object> objectObjectMap = ((Map<Object, Object>[]) getResult)[0];
        assertEquals(3, objectObjectMap.size());
        Object getResult2 = objectObjectMap.get("Resource");
        assertTrue(getResult2 instanceof String[]);
        assertTrue(getResult instanceof Map[]);
        assertEquals("2012-10-17", policyDocument.get("Version"));
        assertEquals("Deny", objectObjectMap.get("Effect"));
        assertEquals("JaneDoe", actualAuthorizationPolicyDocument.getPrincipalId());
        assertEquals("execute-api:Invoke", objectObjectMap.get("Action"));
        assertEquals(1, ((Map<Object, Object>[]) getResult).length);
        assertTrue(actualAuthorizationPolicyDocument.getContext().isEmpty());
        assertArrayEquals(new String[]{"Method Arn/*/*"}, (String[]) getResult2);
    }

    @Test
    @DisplayName("Test getAuthorizedDocument(AuthenticationStatus, String); then return PolicyDocument size is two")
    void testGetAuthorizationPolicyDocumentThenReturnPolicyDocumentSizeIsTwo() throws PolicyGenerationFailedException,
            JwtClaimsNotApplicableException {
        // Arrange
        PolicyConfig policyConfig = mock(PolicyConfig.class);
        when(policyConfig.unauthorizedPrincipal()).thenReturn("JaneDoe");
        AuthorizerConfig authorizerConfig = mock(AuthorizerConfig.class);
        when(authorizerConfig.policy()).thenReturn(policyConfig);
        ApplicationConfig applicationConfig = mock(ApplicationConfig.class);
        when(applicationConfig.authorizer()).thenReturn(authorizerConfig);

        // Act
        IAMPolicyProvider iamPolicyProvider = new IAMPolicyProvider(applicationConfig);
        iamPolicyProvider.objectMapper = new ObjectMapper();
        AuthorizationPolicyResponse actualAuthorizationPolicyDocument = iamPolicyProvider
                .getAuthorizationPolicyDocument(
                        AuthenticationStatus.AUTHENTICATED.withClaimSet(new JWTClaimsSet.Builder()
                                        .subject("JaneDoe")
                                        .audience("none")
                                .build()),
                        "Method Arn"
                );

        // Assert
        verify(applicationConfig).authorizer();
        verify(authorizerConfig).policy();
        Map<String, Object> policyDocument = actualAuthorizationPolicyDocument.getPolicyDocument();
        assertEquals(2, policyDocument.size());
        Object getResult = policyDocument.get("Statement");
        Map<Object, Object> objectObjectMap = ((Map<Object, Object>[]) getResult)[0];
        assertEquals(3, objectObjectMap.size());
        Object getResult2 = objectObjectMap.get("Resource");
        assertTrue(getResult2 instanceof String[]);
        assertTrue(getResult instanceof Map[]);
        assertEquals("2012-10-17", policyDocument.get("Version"));
        assertEquals("Allow", objectObjectMap.get("Effect"));
        assertEquals("JaneDoe", actualAuthorizationPolicyDocument.getPrincipalId());
        assertEquals("execute-api:Invoke", objectObjectMap.get("Action"));
        assertEquals(1, ((Map<Object, Object>[]) getResult).length);
        assertFalse(actualAuthorizationPolicyDocument.getContext().isEmpty());
        assertArrayEquals(new String[]{"Method Arn/*/*"}, (String[]) getResult2);
    }

    @Test
    void testGetAuthorizationPolicyDocumentWithZeroClaims() throws PolicyGenerationFailedException,
            JwtClaimsNotApplicableException, ParseException {
        // Arrange
        PolicyConfig policyConfig = mock(PolicyConfig.class);
        when(policyConfig.unauthorizedPrincipal()).thenReturn("JaneDoe");
        AuthorizerConfig authorizerConfig = mock(AuthorizerConfig.class);
        when(authorizerConfig.policy()).thenReturn(policyConfig);
        ApplicationConfig applicationConfig = mock(ApplicationConfig.class);
        when(applicationConfig.authorizer()).thenReturn(authorizerConfig);

        // Act
        IAMPolicyProvider iamPolicyProvider = new IAMPolicyProvider(applicationConfig);
        iamPolicyProvider.objectMapper = new ObjectMapper();
        AuthorizationPolicyResponse actualAuthorizationPolicyDocument = iamPolicyProvider
                .getAuthorizationPolicyDocument(
                        AuthenticationStatus.AUTHENTICATED.withClaimSet(JWTClaimsSet.parse(new HashMap<>())),
                        "Method Arn"
                );

        // Assert
        verify(applicationConfig).authorizer();
        verify(authorizerConfig).policy();
        Map<String, Object> policyDocument = actualAuthorizationPolicyDocument.getPolicyDocument();
        assertEquals(2, policyDocument.size());
        Object getResult = policyDocument.get("Statement");
        Map<Object, Object> objectObjectMap = ((Map<Object, Object>[]) getResult)[0];
        assertEquals(3, objectObjectMap.size());
        Object getResult2 = objectObjectMap.get("Resource");
        assertTrue(getResult2 instanceof String[]);
        assertTrue(getResult instanceof Map[]);
        assertEquals("2012-10-17", policyDocument.get("Version"));
        assertEquals("Allow", objectObjectMap.get("Effect"));
        assertNull(actualAuthorizationPolicyDocument.getPrincipalId());
        assertEquals("execute-api:Invoke", objectObjectMap.get("Action"));
        assertEquals(1, ((Map<Object, Object>[]) getResult).length);
        assertTrue(actualAuthorizationPolicyDocument.getContext().isEmpty());
        assertArrayEquals(new String[]{"Method Arn/*/*"}, (String[]) getResult2);
    }

    /**
     * Test {@link IAMPolicyProvider#getContextFromJwtPayloadClaims(Map)}.
     * <ul>
     *   <li>When {@link HashMap#HashMap()} {@code foo} is {@code 42}.</li>
     *   <li>Then return size is one.</li>
     * </ul>
     * <p>
     * Method under test:
     * {@link IAMPolicyProvider#getContextFromJwtPayloadClaims(Map)}
     */
    @Test
    @DisplayName("Test getContextFromJwtPayloadClaims(Map); when HashMap() 'foo' is '42'; then return size is one")
    void testGetContextFromJwtPayloadClaimsWhenHashMapFooIs42ThenReturnSizeIsOne()
            throws PolicyGenerationFailedException {
        // Arrange
        AuthorizerConfig authorizerConfig = mock(AuthorizerConfig.class);
        when(authorizerConfig.policy()).thenReturn(mock(PolicyConfig.class));
        ApplicationConfig applicationConfig = mock(ApplicationConfig.class);
        when(applicationConfig.authorizer()).thenReturn(authorizerConfig);
        IAMPolicyProvider iamPolicyProvider = new IAMPolicyProvider(applicationConfig);

        HashMap<String, Object> claims = new HashMap<>();
        claims.put("foo", "42");

        // Act
        Map<String, String> actualContextFromJwtPayloadClaims = iamPolicyProvider
                .getContextFromJwtPayloadClaims(claims);

        // Assert
        verify(applicationConfig).authorizer();
        verify(authorizerConfig).policy();
        assertEquals(1, actualContextFromJwtPayloadClaims.size());
        assertEquals("42", actualContextFromJwtPayloadClaims.get("foo"));
    }

    /**
     * Test {@link IAMPolicyProvider#getContextFromJwtPayloadClaims(Map)}.
     * <ul>
     *   <li>When {@link HashMap#HashMap()}.</li>
     *   <li>Then return Empty.</li>
     * </ul>
     * <p>
     * Method under test:
     * {@link IAMPolicyProvider#getContextFromJwtPayloadClaims(Map)}
     */
    @Test
    @DisplayName("Test getContextFromJwtPayloadClaims(Map); when HashMap(); then return Empty")
    void testGetContextFromJwtPayloadClaimsWhenHashMapThenReturnEmpty() throws PolicyGenerationFailedException {
        // Arrange
        AuthorizerConfig authorizerConfig = mock(AuthorizerConfig.class);
        when(authorizerConfig.policy()).thenReturn(mock(PolicyConfig.class));
        ApplicationConfig applicationConfig = mock(ApplicationConfig.class);
        when(applicationConfig.authorizer()).thenReturn(authorizerConfig);
        IAMPolicyProvider iamPolicyProvider = new IAMPolicyProvider(applicationConfig);

        // Act
        Map<String, String> actualContextFromJwtPayloadClaims = iamPolicyProvider
                .getContextFromJwtPayloadClaims(new HashMap<>());

        // Assert
        verify(applicationConfig).authorizer();
        verify(authorizerConfig).policy();
        assertTrue(actualContextFromJwtPayloadClaims.isEmpty());
    }

    /**
     * Test {@link IAMPolicyProvider#getResourceFromMethodArn(String)}.
     * <ul>
     *   <li>When {@code Method Arn}.</li>
     *   <li>Then return {@code Method Arn/* /*}.</li>
     * </ul>
     * <p>
     * Method under test: {@link IAMPolicyProvider#getResourceFromMethodArn(String)}
     */
    @Test
    @DisplayName("Test getResourceFromMethodArn(String); when 'Method Arn'; then return 'Method Arn/*/*'")
    void testGetResourceFromMethodArnWhenMethodArnThenReturnMethodArn() {
        // Arrange, Act and Assert
        assertEquals("Method Arn/*/*", IAMPolicyProvider.getResourceFromMethodArn("Method Arn"));
    }

    /**
     * Test {@link IAMPolicyProvider#IAMPolicyProvider(ApplicationConfig)}.
     * <p>
     * Method under test:
     * {@link IAMPolicyProvider#IAMPolicyProvider(ApplicationConfig)}
     */
    @Test
    @DisplayName("Test new IAMPolicyProvider(ApplicationConfig)")
    void testNewIAMPolicyProvider() {
        // Arrange
        AuthorizerConfig authorizerConfig = mock(AuthorizerConfig.class);
        when(authorizerConfig.policy()).thenReturn(mock(PolicyConfig.class));
        ApplicationConfig applicationConfig = mock(ApplicationConfig.class);
        when(applicationConfig.authorizer()).thenReturn(authorizerConfig);

        // Act
        IAMPolicyProvider actualIamPolicyProvider = new IAMPolicyProvider(applicationConfig);

        // Assert
        verify(applicationConfig).authorizer();
        verify(authorizerConfig).policy();
        assertNull(actualIamPolicyProvider.objectMapper);
    }
}
