package com.yourkoder.letsmeet.iam.authorizer.token;

import com.nimbusds.jwt.JWTClaimsSet;
import com.yourkoder.letsmeet.iam.authorizer.token.exception.JwtClaimsNotApplicableException;
import com.yourkoder.letsmeet.iam.authorizer.token.exception.JwtExpiredException;
import com.yourkoder.letsmeet.iam.authorizer.token.valueobject.AuthenticationStatus;
import com.yourkoder.letsmeet.iam.authorizer.token.valueobject.AuthorizationBearerToken;
import com.yourkoder.letsmeet.iam.authorizer.token.valueobject.JWTType;
import com.yourkoder.letsmeet.iam.sdk.model.WhitelistedToken;
import com.yourkoder.letsmeet.iam.sdk.repository.WhitelistedTokenRepository;
import com.yourkoder.letsmeet.iam.sdk.service.JwtService;
import com.yourkoder.letsmeet.iam.sdk.service.exception.JWTVerificationFailedException;
import com.yourkoder.letsmeet.iam.sdk.util.keymanagement.ParameterNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@ApplicationScoped
@Default
@JBossLog
public class TokenAuthorizer {
    public static final String NO_AUTH_TOKEN = "None";

    @Inject
    JwtService jwtService;

    @Inject
    WhitelistedTokenRepository whitelistedTokenRepository;

    public AuthenticationStatus verifyAuthenticationToken(
            AuthorizationBearerToken authorizationToken
    ) throws JwtClaimsNotApplicableException {
        LOG.debugf("Authenticating user based on provided token: %s", authorizationToken.toString());

        if (authorizationToken.getJwtType().equals(JWTType.NONE)
                && authorizationToken.getValue().equalsIgnoreCase(NO_AUTH_TOKEN)) {
            return AuthenticationStatus.AUTHENTICATED.withClaimSet(
                    new JWTClaimsSet.Builder().subject("Anonymous").audience("none").build()
            );
        }

        LOG.debugf("Authorization token: [%s] has a valid format", authorizationToken.toString());

        if (authorizationToken.getValue().equalsIgnoreCase(NO_AUTH_TOKEN)) {
            JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                    .subject("Anonymous")
                    .audience("none")
                    .build();

            return AuthenticationStatus.AUTHENTICATED.withClaimSet(jwtClaimsSet);
        }

        final JWTClaimsSet jwtClaimsSet;
        try {
            if (authorizationToken.getJwtType().equals(JWTType.JWS)) {
                jwtClaimsSet = jwtService.verifyAndGetAccessTokenClaimSet(authorizationToken.getValue());
            } else {
                LOG.error("JWE Verification not support. Can not verify token.");
                return AuthenticationStatus.UNAUTHENTICATED;
            }
        } catch (JWTVerificationFailedException | ParameterNotFoundException e) {
            LOG.error(e);
            LOG.error(e.getMessage(), e);
            return AuthenticationStatus.UNAUTHENTICATED;
        }

        try {
            verifyExpiration(jwtClaimsSet);
        } catch (JwtExpiredException e) {
            LOG.error(e.getMessage());
            return AuthenticationStatus.UNAUTHENTICATED;
        }

        if (jwtClaimsSet.getSubject() == null || jwtClaimsSet.getSubject().isBlank()) {
            return AuthenticationStatus.UNAUTHENTICATED;
        }

        LOG.debugf("Verifying token is whitelisted.");
        final Optional<WhitelistedToken> whitelistTokenOptional = whitelistedTokenRepository.findById(
                jwtClaimsSet.getSubject(),
                jwtClaimsSet.getJWTID()
        );

        if (whitelistTokenOptional.isEmpty()) {
            LOG.errorf(
                    "Token provided to authorizer with token type: [%s] and jit: [%s] "
                            + "was not whitelisted. Can not authenticate.",
                    jwtClaimsSet.getSubject(),
                    jwtClaimsSet.getJWTID()
            );
            return AuthenticationStatus.UNAUTHENTICATED;
        }
        LOG.debugf("Found whitelisted token: [%s]", whitelistTokenOptional.get().getTokenID());

        return AuthenticationStatus.AUTHENTICATED.withClaimSet(jwtClaimsSet);

    }

    private void verifyExpiration(JWTClaimsSet jwtClaimsSet) throws JwtExpiredException {
        Date now = Date.from(Instant.now());

        Date notBeforeTime = jwtClaimsSet.getNotBeforeTime();
        if (notBeforeTime == null) {
            throw new JwtExpiredException("Token does not have a not-before-time claim. Token can not be used.");
        }
        if (notBeforeTime.after(now)) {
            throw new JwtExpiredException("Token can not be used currently");
        }

        Date expirationTime = jwtClaimsSet.getExpirationTime();
        if (expirationTime.before(now)) {
            throw new JwtExpiredException("Token expired");
        }
    }
}
