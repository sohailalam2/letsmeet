package com.yourkoder.letsmeet.iam.authorizer.token;

import com.nimbusds.jwt.JWTClaimsSet;
import com.yourkoder.letsmeet.iam.authorizer.token.valueobject.AuthenticationStatus;
import com.yourkoder.letsmeet.iam.authorizer.token.valueobject.AuthorizationBearerToken;
import com.yourkoder.letsmeet.iam.authorizer.token.valueobject.JWTType;
import com.yourkoder.letsmeet.iam.sdk.model.WhitelistedToken;
import com.yourkoder.letsmeet.iam.sdk.repository.WhitelistedTokenRepository;
import com.yourkoder.letsmeet.iam.sdk.service.JwtService;
import com.yourkoder.letsmeet.iam.sdk.service.exception.JWTVerificationFailedException;
import com.yourkoder.letsmeet.iam.sdk.util.keymanagement.ParameterNotFoundException;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.inject.Inject;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@QuarkusTest
@Tag("unit")
class TokenAuthorizerTest {

    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.signature";

    private static final String NO_AUTH_TOKEN = "None";

    private static final String JTI = "test-jti";

    private static final String SUBJECT = "test-user";

    @Inject
    TokenAuthorizer tokenAuthorizer;

    private JwtService jwtService;

    private WhitelistedTokenRepository whitelistedTokenRepository;

    @BeforeEach
    void setUp() {
        jwtService = Mockito.mock(JwtService.class);
        whitelistedTokenRepository = Mockito.mock(WhitelistedTokenRepository.class);

        QuarkusMock.installMockForType(jwtService, JwtService.class);
        QuarkusMock.installMockForType(whitelistedTokenRepository, WhitelistedTokenRepository.class);
    }

    @Test
    void verifyAuthenticationTokenWithNoAuthTokenAndNoneTypeReturnsAuthenticated() throws Exception {
        AuthorizationBearerToken token = new AuthorizationBearerToken("Bearer " + NO_AUTH_TOKEN);
        AuthenticationStatus result = tokenAuthorizer.verifyAuthenticationToken(token);

        assertEquals(AuthenticationStatus.AUTHENTICATED, result);
        assertEquals("Anonymous", result.getJwtClaimsSet().getSubject());
        assertEquals("none", result.getJwtClaimsSet().getAudience().get(0));
    }

    @Test
    void verifyAuthenticationTokenWithNoAuthTokenAndNonNoneTypeReturnsAuthenticated() throws Exception {
        AuthorizationBearerToken token = new AuthorizationBearerToken("Bearer " + NO_AUTH_TOKEN);
        // Simulate JWS type for this case
        JWTType jwtType = JWTType.JWS;
        AuthorizationBearerToken spiedToken = spy(token);
        when(spiedToken.getJwtType()).thenReturn(jwtType);

        AuthenticationStatus result = tokenAuthorizer.verifyAuthenticationToken(spiedToken);

        assertEquals(AuthenticationStatus.AUTHENTICATED, result);
        assertEquals("Anonymous", result.getJwtClaimsSet().getSubject());
        assertEquals("none", result.getJwtClaimsSet().getAudience().get(0));
    }

    @Test
    void verifyAuthenticationTokenWithJWETypeReturnsUnauthenticated() throws Exception {
        AuthorizationBearerToken token = new AuthorizationBearerToken("Bearer " + VALID_TOKEN);
        AuthorizationBearerToken spiedToken = spy(token);
        when(spiedToken.getJwtType()).thenReturn(JWTType.JWE);

        AuthenticationStatus result = tokenAuthorizer.verifyAuthenticationToken(spiedToken);

        assertEquals(AuthenticationStatus.UNAUTHENTICATED, result);
    }

    @Test
    void verifyAuthenticationTokenWithValidJWSAndWhitelistedTokenReturnsAuthenticated() throws Exception {
        AuthorizationBearerToken token = new AuthorizationBearerToken("Bearer " + VALID_TOKEN);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(SUBJECT)
                .jwtID(JTI)
                .notBeforeTime(Date.from(Instant.now().minusSeconds(60)))
                .expirationTime(Date.from(Instant.now().plusSeconds(60)))
                .build();
        WhitelistedToken whitelistedToken = new WhitelistedToken();
        whitelistedToken.setSkFromTokenID(JTI);

        when(jwtService.verifyAndGetAccessTokenClaimSet(VALID_TOKEN)).thenReturn(claimsSet);
        when(whitelistedTokenRepository.findById(SUBJECT, JTI)).thenReturn(Optional.of(whitelistedToken));

        AuthenticationStatus result = tokenAuthorizer.verifyAuthenticationToken(token);

        assertEquals(AuthenticationStatus.AUTHENTICATED, result);
        assertEquals(claimsSet, result.getJwtClaimsSet());
        verify(whitelistedTokenRepository).findById(SUBJECT, JTI);
    }

    @Test
    void verifyAuthenticationTokenWithJWSAndVerificationFailureReturnsUnauthenticated() throws Exception {
        AuthorizationBearerToken token = new AuthorizationBearerToken("Bearer " + VALID_TOKEN);
        when(jwtService.verifyAndGetAccessTokenClaimSet(VALID_TOKEN))
                .thenThrow(new JWTVerificationFailedException("Verification failed"));

        AuthenticationStatus result = tokenAuthorizer.verifyAuthenticationToken(token);

        assertEquals(AuthenticationStatus.UNAUTHENTICATED, result);
    }

    @Test
    void verifyAuthenticationTokenWithJWSAndParameterNotFoundReturnsUnauthenticated() throws Exception {
        AuthorizationBearerToken token = new AuthorizationBearerToken("Bearer " + VALID_TOKEN);
        when(jwtService.verifyAndGetAccessTokenClaimSet(VALID_TOKEN))
                .thenThrow(new ParameterNotFoundException("Parameter missing"));

        AuthenticationStatus result = tokenAuthorizer.verifyAuthenticationToken(token);

        assertEquals(AuthenticationStatus.UNAUTHENTICATED, result);
    }

    @Test
    void verifyAuthenticationTokenWithExpiredTokenReturnsUnauthenticated() throws Exception {
        AuthorizationBearerToken token = new AuthorizationBearerToken("Bearer " + VALID_TOKEN);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(SUBJECT)
                .jwtID(JTI)
                .notBeforeTime(Date.from(Instant.now().minusSeconds(120)))
                .expirationTime(Date.from(Instant.now().minusSeconds(60)))
                .build();

        when(jwtService.verifyAndGetAccessTokenClaimSet(VALID_TOKEN)).thenReturn(claimsSet);

        AuthenticationStatus result = tokenAuthorizer.verifyAuthenticationToken(token);

        assertEquals(AuthenticationStatus.UNAUTHENTICATED, result);
    }

    @Test
    void verifyAuthenticationTokenWithNotYetValidTokenReturnsUnauthenticated() throws Exception {
        AuthorizationBearerToken token = new AuthorizationBearerToken("Bearer " + VALID_TOKEN);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(SUBJECT)
                .jwtID(JTI)
                .notBeforeTime(Date.from(Instant.now().plusSeconds(60)))
                .expirationTime(Date.from(Instant.now().plusSeconds(120)))
                .build();

        when(jwtService.verifyAndGetAccessTokenClaimSet(VALID_TOKEN)).thenReturn(claimsSet);

        AuthenticationStatus result = tokenAuthorizer.verifyAuthenticationToken(token);

        assertEquals(AuthenticationStatus.UNAUTHENTICATED, result);
    }

    @Test
    void verifyAuthenticationTokenWithMissingNotBeforeTimeThrowsJwtExpiredException() throws Exception {
        AuthorizationBearerToken token = new AuthorizationBearerToken("Bearer " + VALID_TOKEN);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(SUBJECT)
                .jwtID(JTI)
                .expirationTime(Date.from(Instant.now().plusSeconds(60)))
                .build();

        when(jwtService.verifyAndGetAccessTokenClaimSet(VALID_TOKEN)).thenReturn(claimsSet);

        AuthenticationStatus result = tokenAuthorizer.verifyAuthenticationToken(token);

        assertEquals(AuthenticationStatus.UNAUTHENTICATED, result);
    }

    @Test
    void verifyAuthenticationTokenWithNullSubjectReturnsUnauthenticated() throws Exception {
        AuthorizationBearerToken token = new AuthorizationBearerToken("Bearer " + VALID_TOKEN);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .jwtID(JTI)
                .notBeforeTime(Date.from(Instant.now().minusSeconds(60)))
                .expirationTime(Date.from(Instant.now().plusSeconds(60)))
                .build();

        when(jwtService.verifyAndGetAccessTokenClaimSet(VALID_TOKEN)).thenReturn(claimsSet);

        AuthenticationStatus result = tokenAuthorizer.verifyAuthenticationToken(token);

        assertEquals(AuthenticationStatus.UNAUTHENTICATED, result);
    }

    @Test
    void verifyAuthenticationTokenWithBlankSubjectReturnsUnauthenticated() throws Exception {
        AuthorizationBearerToken token = new AuthorizationBearerToken("Bearer " + VALID_TOKEN);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("")
                .jwtID(JTI)
                .notBeforeTime(Date.from(Instant.now().minusSeconds(60)))
                .expirationTime(Date.from(Instant.now().plusSeconds(60)))
                .build();

        when(jwtService.verifyAndGetAccessTokenClaimSet(VALID_TOKEN)).thenReturn(claimsSet);

        AuthenticationStatus result = tokenAuthorizer.verifyAuthenticationToken(token);

        assertEquals(AuthenticationStatus.UNAUTHENTICATED, result);
    }

    @Test
    void verifyAuthenticationTokenWithNonWhitelistedTokenReturnsUnauthenticated() throws Exception {
        AuthorizationBearerToken token = new AuthorizationBearerToken("Bearer " + VALID_TOKEN);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(SUBJECT)
                .jwtID(JTI)
                .notBeforeTime(Date.from(Instant.now().minusSeconds(60)))
                .expirationTime(Date.from(Instant.now().plusSeconds(60)))
                .build();

        when(jwtService.verifyAndGetAccessTokenClaimSet(VALID_TOKEN)).thenReturn(claimsSet);
        when(whitelistedTokenRepository.findById(SUBJECT, JTI)).thenReturn(Optional.empty());

        AuthenticationStatus result = tokenAuthorizer.verifyAuthenticationToken(token);

        assertEquals(AuthenticationStatus.UNAUTHENTICATED, result);
    }
}