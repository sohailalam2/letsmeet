package com.yourkoder.letsmeet.function;

import com.yourkoder.letsmeet.iam.authorizer.policy.IAMPolicyProvider;
import com.yourkoder.letsmeet.iam.authorizer.policy.dtos.response.AuthorizationPolicyResponse;
import com.yourkoder.letsmeet.iam.authorizer.token.TokenAuthorizer;
import com.yourkoder.letsmeet.iam.authorizer.token.valueobject.AuthenticationStatus;
import com.yourkoder.letsmeet.iam.authorizer.token.valueobject.AuthorizationBearerToken;
import com.yourkoder.letsmeet.iam.authorizer.token.valueobject.AuthorizerLambdaEventPayloadType;
import com.yourkoder.letsmeet.iam.authorizer.token.valueobject.exception.InvalidJwtFormatException;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@Tag("unit")
class ApplicationMainLambdaTest {

    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.signature";

    private static final String METHOD_ARN = "arn:aws:execute-api:region:account:api-id/stage/GET/resource";

    private static final String NO_AUTH_TOKEN = "None";

    @Inject
    ApplicationMainLambda applicationMainLambda;

    private TokenAuthorizer tokenAuthorizer;

    private IAMPolicyProvider policyProvider;

    @BeforeEach
    void setUp() {
        tokenAuthorizer = Mockito.mock(TokenAuthorizer.class);
        policyProvider = Mockito.mock(IAMPolicyProvider.class);

        QuarkusMock.installMockForType(tokenAuthorizer, TokenAuthorizer.class);
        QuarkusMock.installMockForType(policyProvider, IAMPolicyProvider.class);
    }

    @Test
    void handleRequestWithTokenEventAndValidTokenReturnsPolicy() throws Exception {
        Map<String, Object> event = new HashMap<>();
        event.put("type", AuthorizerLambdaEventPayloadType.TOKEN_TYPE);
        event.put("authorizationToken", "Bearer " + VALID_TOKEN);
        event.put("methodArn", METHOD_ARN);
        AuthenticationStatus status = AuthenticationStatus.AUTHENTICATED;
        AuthorizationPolicyResponse policyResponse = new AuthorizationPolicyResponse();

        when(tokenAuthorizer.verifyAuthenticationToken(any(AuthorizationBearerToken.class))).thenReturn(status);
        when(policyProvider.getAuthorizationPolicyDocument(status, METHOD_ARN)).thenReturn(policyResponse);

        AuthorizationPolicyResponse result = applicationMainLambda.handleRequest(event);

        assertEquals(policyResponse, result);
        verify(tokenAuthorizer).verifyAuthenticationToken(any(AuthorizationBearerToken.class));
        verify(policyProvider).getAuthorizationPolicyDocument(status, METHOD_ARN);
    }

    @Test
    void handleRequestWithTokenEventAndInvalidTokenThrowsInvalidJwtFormatException() throws Exception {
        Map<String, Object> event = new HashMap<>();
        event.put("type", AuthorizerLambdaEventPayloadType.TOKEN_TYPE);
        event.put("authorizationToken", "Invalid");
        event.put("methodArn", METHOD_ARN);

        assertThrows(InvalidJwtFormatException.class, () -> applicationMainLambda.handleRequest(event));
    }

    @Test
    void handleRequestWithRequestEventAndAuthorizationHeaderReturnsPolicy() throws Exception {
        Map<String, Object> event = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + VALID_TOKEN);
        event.put("type", AuthorizerLambdaEventPayloadType.REQUEST_TYPE);
        event.put("headers", headers);
        event.put("methodArn", METHOD_ARN);
        AuthenticationStatus status = AuthenticationStatus.AUTHENTICATED;
        AuthorizationPolicyResponse policyResponse = new AuthorizationPolicyResponse();

        when(tokenAuthorizer.verifyAuthenticationToken(any(AuthorizationBearerToken.class))).thenReturn(status);
        when(policyProvider.getAuthorizationPolicyDocument(status, METHOD_ARN)).thenReturn(policyResponse);

        AuthorizationPolicyResponse result = applicationMainLambda.handleRequest(event);

        assertEquals(policyResponse, result);
        verify(tokenAuthorizer).verifyAuthenticationToken(any(AuthorizationBearerToken.class));
        verify(policyProvider).getAuthorizationPolicyDocument(status, METHOD_ARN);
    }

    @Test
    void handleRequestWithRequestEventAndNoAuthTokenReturnsPolicy() throws Exception {
        Map<String, Object> event = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        event.put("type", AuthorizerLambdaEventPayloadType.REQUEST_TYPE);
        event.put("headers", headers);
        event.put("methodArn", METHOD_ARN);
        AuthenticationStatus status = AuthenticationStatus.AUTHENTICATED;
        AuthorizationPolicyResponse policyResponse = new AuthorizationPolicyResponse();

        when(tokenAuthorizer.verifyAuthenticationToken(any(AuthorizationBearerToken.class))).thenReturn(status);
        when(policyProvider.getAuthorizationPolicyDocument(status, METHOD_ARN)).thenReturn(policyResponse);

        AuthorizationPolicyResponse result = applicationMainLambda.handleRequest(event);

        assertEquals(policyResponse, result);
        verify(tokenAuthorizer).verifyAuthenticationToken(argThat(token -> token.getValue().equals(NO_AUTH_TOKEN)));
    }

    @Test
    void handleRequestWithWebSocketEventAndAuthHeaderReturnsPolicy() throws Exception {
        Map<String, Object> event = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + VALID_TOKEN);
        headers.put("Upgrade", "websocket");
        event.put("type", AuthorizerLambdaEventPayloadType.REQUEST_TYPE);
        event.put("headers", headers);
        event.put("methodArn", METHOD_ARN);
        AuthenticationStatus status = AuthenticationStatus.AUTHENTICATED;
        AuthorizationPolicyResponse policyResponse = new AuthorizationPolicyResponse();

        when(tokenAuthorizer.verifyAuthenticationToken(any(AuthorizationBearerToken.class))).thenReturn(status);
        when(policyProvider.getAuthorizationPolicyDocument(status, METHOD_ARN)).thenReturn(policyResponse);

        AuthorizationPolicyResponse result = applicationMainLambda.handleRequest(event);

        assertEquals(policyResponse, result);
        verify(tokenAuthorizer).verifyAuthenticationToken(any(AuthorizationBearerToken.class));
    }

    @Test
    void handleRequestWithWebSocketEventAndQueryParamTokenReturnsPolicy() throws Exception {
        Map<String, Object> event = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        Map<String, Object> queryParams = new HashMap<>();
        headers.put("Upgrade", "websocket");
        queryParams.put("auth_token", VALID_TOKEN);
        event.put("type", AuthorizerLambdaEventPayloadType.REQUEST_TYPE);
        event.put("headers", headers);
        event.put("queryStringParameters", queryParams);
        event.put("methodArn", METHOD_ARN);
        AuthenticationStatus status = AuthenticationStatus.AUTHENTICATED;
        AuthorizationPolicyResponse policyResponse = new AuthorizationPolicyResponse();

        when(tokenAuthorizer.verifyAuthenticationToken(any(AuthorizationBearerToken.class))).thenReturn(status);
        when(policyProvider.getAuthorizationPolicyDocument(status, METHOD_ARN)).thenReturn(policyResponse);

        AuthorizationPolicyResponse result = applicationMainLambda.handleRequest(event);

        assertEquals(policyResponse, result);
        verify(tokenAuthorizer).verifyAuthenticationToken(any(AuthorizationBearerToken.class));
    }

    @Test
    void handleRequestWithMissingArnThrowsRuntimeException() {
        Map<String, Object> event = new HashMap<>();
        event.put("type", AuthorizerLambdaEventPayloadType.REQUEST_TYPE);

        assertThrows(RuntimeException.class, () -> applicationMainLambda.handleRequest(event),
                "Can not authorize request. No route or method arn available in event");
    }

    @Test
    void handleRequestWithRouteArnReturnsPolicy() throws Exception {
        Map<String, Object> event = new HashMap<>();
        event.put("type", AuthorizerLambdaEventPayloadType.TOKEN_TYPE);
        event.put("authorizationToken", "Bearer " + VALID_TOKEN);
        event.put("routeArn", METHOD_ARN);
        AuthenticationStatus status = AuthenticationStatus.AUTHENTICATED;
        AuthorizationPolicyResponse policyResponse = new AuthorizationPolicyResponse();

        when(tokenAuthorizer.verifyAuthenticationToken(any(AuthorizationBearerToken.class))).thenReturn(status);
        when(policyProvider.getAuthorizationPolicyDocument(status, METHOD_ARN)).thenReturn(policyResponse);

        AuthorizationPolicyResponse result = applicationMainLambda.handleRequest(event);

        assertEquals(policyResponse, result);
        verify(policyProvider).getAuthorizationPolicyDocument(status, METHOD_ARN);
    }
}