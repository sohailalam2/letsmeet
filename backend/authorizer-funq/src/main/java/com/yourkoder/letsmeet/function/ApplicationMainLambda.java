package com.yourkoder.letsmeet.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourkoder.letsmeet.iam.authorizer.policy.IAMPolicyProvider;
import com.yourkoder.letsmeet.iam.authorizer.policy.dtos.response.AuthorizationPolicyResponse;
import com.yourkoder.letsmeet.iam.authorizer.token.TokenAuthorizer;
import com.yourkoder.letsmeet.iam.authorizer.token.exception.JwtClaimsNotApplicableException;
import com.yourkoder.letsmeet.iam.authorizer.token.valueobject.AuthenticationStatus;
import com.yourkoder.letsmeet.iam.authorizer.token.valueobject.AuthorizationBearerToken;
import com.yourkoder.letsmeet.iam.authorizer.token.valueobject.AuthorizerLambdaEventPayloadType;
import com.yourkoder.letsmeet.iam.authorizer.token.valueobject.exception.InvalidJwtFormatException;
import io.quarkus.funqy.Funq;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.jbosslog.JBossLog;

import java.util.Locale;
import java.util.Map;

@JBossLog
public class ApplicationMainLambda {

    private static final String HEADERS_PROPERTY = "headers";

    private static final String AUTHORIZATION_HEADER_PROPERTY = "Authorization";

    private static final String METHOD_ARN_EVENT_PROPERTY = "methodArn";

    private static final String ROUTE_ARN_EVENT_PROPERTY = "routeArn";

    private static final String TYPE_PROPERTY = "type";

    private static final String AUTHORIZATION_TOKEN_EVENT_PROPERTY = "authorizationToken";

    @Inject
    TokenAuthorizer tokenAuthorizer;

    @Inject
    IAMPolicyProvider policyProvider;

    @Inject
    ObjectMapper objectMapper;

    @Funq
    @SneakyThrows
    public AuthorizationPolicyResponse handleRequest(Map<String, Object> event) {
        try {
            if (LOG.isDebugEnabled()) {
                String serialisedEvent = objectMapper.writeValueAsString(event);
                LOG.debug("Handler authorizer event:");
                LOG.debug(serialisedEvent);
            }

            AuthenticationStatus authenticationStatus = AuthenticationStatus.UNAUTHENTICATED;

            if (isRequestLambdaEventPayload(event)) {
                LOG.debugf("Authorization event is of type: [%s]",
                        AuthorizerLambdaEventPayloadType.REQUEST_TYPE);

                final Map<String, Object> headers = (Map<String, Object>) event.get(HEADERS_PROPERTY);

                if (headers != null) {
                    if (containsHeader(headers, AUTHORIZATION_HEADER_PROPERTY)) {
                        authenticationStatus = getAuthenticationStatusFromAuthorizationHeader(headers);
                    } else {
                        authenticationStatus = tokenAuthorizer.verifyAuthenticationToken(
                                AuthorizationBearerToken.fromTokenString(TokenAuthorizer.NO_AUTH_TOKEN)
                        );
                    }
                }
            } else if (isTokenLambdaEventPayload(event)) { // REST Authorization Event
                LOG.debugf("Authorization event is of type: [%s]",
                        AuthorizerLambdaEventPayloadType.TOKEN_TYPE);
                LOG.debug("Received REST Authorization Event.");
                final String authorisationToken = ((String) event.get(AUTHORIZATION_TOKEN_EVENT_PROPERTY)).trim();
                authenticationStatus = tokenAuthorizer.verifyAuthenticationToken(
                        new AuthorizationBearerToken(authorisationToken)
                );
            }

            final String methodArn;
            if (event.containsKey(METHOD_ARN_EVENT_PROPERTY)) {
                methodArn = (String) event.get(METHOD_ARN_EVENT_PROPERTY);
            } else if (event.containsKey(ROUTE_ARN_EVENT_PROPERTY)) {
                methodArn = (String) event.get(ROUTE_ARN_EVENT_PROPERTY);
            } else {
                throw new RuntimeException("Can not authorize request. No route or method arn available in event: [%s]"
                        .formatted(event));
            }

            final AuthorizationPolicyResponse authorizationPolicyResponse = policyProvider
                    .getAuthorizationPolicyDocument(authenticationStatus, methodArn);

            if (LOG.isDebugEnabled()) {
                final String serialisedAuthorizationPolicyResponse
                        = objectMapper.writeValueAsString(authorizationPolicyResponse);
                LOG.debugf("Returning authorizer policy response: [%n%s%n]",
                        serialisedAuthorizationPolicyResponse);
            }

            return authorizationPolicyResponse;
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    private AuthenticationStatus getAuthenticationStatusFromAuthorizationHeader(
            Map<String, Object> headers
    ) throws JwtClaimsNotApplicableException {
        final String authorisationToken = ((String) getHeader(headers, AUTHORIZATION_HEADER_PROPERTY)).trim();
        try {
            return  tokenAuthorizer.verifyAuthenticationToken(
                    new AuthorizationBearerToken(authorisationToken)
            );
        } catch (InvalidJwtFormatException e) {
            LOG.error(e.getMessage(), e);
            return AuthenticationStatus.UNAUTHENTICATED;
        }
    }

    private boolean isTokenLambdaEventPayload(Map<String, Object> event) {
        return AuthorizerLambdaEventPayloadType.TOKEN_TYPE.equals(event.get(TYPE_PROPERTY));
    }

    private boolean isRequestLambdaEventPayload(Map<String, Object> event) {
        return AuthorizerLambdaEventPayloadType.REQUEST_TYPE.equals(event.get(TYPE_PROPERTY));
    }

    private static Object getHeader(Map<String, Object> headers, String header) {
        return headers.containsKey(header) ? headers.get(header) : headers.get(header.toLowerCase(Locale.ROOT));
    }

    private static boolean containsHeader(Map<String, Object> headers, String header) {
        return headers.containsKey(header) || headers.containsKey(header.toLowerCase(Locale.ROOT));
    }

}
