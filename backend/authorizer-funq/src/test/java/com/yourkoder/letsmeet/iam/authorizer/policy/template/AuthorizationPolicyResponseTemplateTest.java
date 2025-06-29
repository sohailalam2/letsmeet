package com.yourkoder.letsmeet.iam.authorizer.policy.template;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.yourkoder.letsmeet.iam.authorizer.policy.dtos.response.AuthorizationPolicyResponse;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthorizationPolicyResponseTemplateTest {
    /**
     * Test
     * {@link AuthorizationPolicyResponseTemplate#getAuthorizationPolicyResponse()}.
     * <ul>
     *   <li>Then return first element {@code Effect} is
     * {@link AuthorizationPolicyResponseTemplate#ALLOW}.</li>
     * </ul>
     * <p>
     * Method under test:
     * {@link AuthorizationPolicyResponseTemplate#getAuthorizationPolicyResponse()}
     */
    @Test
    @DisplayName("Test getAuthorizationPolicyResponse(); then return first element 'Effect' is ALLOW")
    void testGetAuthorizationPolicyResponseThenReturnFirstElementEffectIsAllow() {
        // Arrange and Act
        AuthorizationPolicyResponse actualAuthorizationPolicyResponse = (new AuthorizationPolicyResponseTemplate("42",
                "Resource", true, new HashMap<>())).getAuthorizationPolicyResponse();

        // Assert
        Map<String, Object> policyDocument = actualAuthorizationPolicyResponse.getPolicyDocument();
        assertEquals(2, policyDocument.size());
        Object getResult = policyDocument.get("Statement");
        Map<Object, Object> objectObjectMap = ((Map<Object, Object>[]) getResult)[0];
        assertEquals(3, objectObjectMap.size());
        Object getResult2 = objectObjectMap.get("Resource");
        assertTrue(getResult2 instanceof String[]);
        assertTrue(getResult instanceof Map[]);
        assertEquals("42", actualAuthorizationPolicyResponse.getPrincipalId());
        assertEquals(1, ((Map<Object, Object>[]) getResult).length);
        assertTrue(actualAuthorizationPolicyResponse.getContext().isEmpty());
        assertEquals(AuthorizationPolicyResponseTemplate.ALLOW, objectObjectMap.get("Effect"));
        assertEquals(AuthorizationPolicyResponseTemplate.EXECUTE_API_INVOKE, objectObjectMap.get("Action"));
        assertEquals(AuthorizationPolicyResponseTemplate.VERSION_2012_10_17, policyDocument.get("Version"));
        assertArrayEquals(new String[]{"Resource"}, (String[]) getResult2);
    }

    /**
     * Test
     * {@link AuthorizationPolicyResponseTemplate#getAuthorizationPolicyResponse()}.
     * <ul>
     *   <li>Then return first element {@code Effect} is
     * {@link AuthorizationPolicyResponseTemplate#DENY}.</li>
     * </ul>
     * <p>
     * Method under test:
     * {@link AuthorizationPolicyResponseTemplate#getAuthorizationPolicyResponse()}
     */
    @Test
    @DisplayName("Test getAuthorizationPolicyResponse(); then return first element 'Effect' is DENY")
    void testGetAuthorizationPolicyResponseThenReturnFirstElementEffectIsDeny() {
        // Arrange and Act
        AuthorizationPolicyResponse actualAuthorizationPolicyResponse = (new AuthorizationPolicyResponseTemplate("42",
                "Resource", false, new HashMap<>())).getAuthorizationPolicyResponse();

        // Assert
        Map<String, Object> policyDocument = actualAuthorizationPolicyResponse.getPolicyDocument();
        assertEquals(2, policyDocument.size());
        Object getResult = policyDocument.get("Statement");
        Map<Object, Object> objectObjectMap = ((Map<Object, Object>[]) getResult)[0];
        assertEquals(3, objectObjectMap.size());
        Object getResult2 = objectObjectMap.get("Resource");
        assertTrue(getResult2 instanceof String[]);
        assertTrue(getResult instanceof Map[]);
        assertEquals("42", actualAuthorizationPolicyResponse.getPrincipalId());
        assertEquals(1, ((Map<Object, Object>[]) getResult).length);
        assertTrue(actualAuthorizationPolicyResponse.getContext().isEmpty());
        assertEquals(AuthorizationPolicyResponseTemplate.DENY, objectObjectMap.get("Effect"));
        assertEquals(AuthorizationPolicyResponseTemplate.EXECUTE_API_INVOKE, objectObjectMap.get("Action"));
        assertEquals(AuthorizationPolicyResponseTemplate.VERSION_2012_10_17, policyDocument.get("Version"));
        assertArrayEquals(new String[]{"Resource"}, (String[]) getResult2);
    }
}
