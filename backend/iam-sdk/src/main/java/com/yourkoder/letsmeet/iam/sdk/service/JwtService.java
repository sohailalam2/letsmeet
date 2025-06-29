package com.yourkoder.letsmeet.iam.sdk.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.yourkoder.letsmeet.iam.sdk.service.exception.JWTVerificationFailedException;
import com.yourkoder.letsmeet.iam.sdk.util.keymanagement.ParameterNotFoundException;
import com.yourkoder.letsmeet.iam.sdk.util.keymanagement.ParameterStoreService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;

@JBossLog
@ApplicationScoped
public class JwtService {

    public static final String AUTH_ISSUER = "http://auth.letsmeet.com";

    public static final String AUTH_AUDIENCE = "http://auth.letsmeet.com";

    public static final String API_AUDIENCE = "http://api.letsmeet.com";

    @Inject
    protected ParameterStoreService parameterStoreService;

    public JWTClaimsSet verifyAndGetAccessTokenClaimSet(String token)
            throws JWTVerificationFailedException, ParameterNotFoundException {

        JWTClaimsSet jwtClaimsSet = verifyAndGetClaimSet(token);
        if (!jwtClaimsSet.getIssuer().equals(AUTH_ISSUER)) {
            throw new JWTVerificationFailedException("Invalid access token issuer.");
        }

        if (jwtClaimsSet.getAudience().isEmpty() || !jwtClaimsSet.getAudience().getFirst().equals(API_AUDIENCE)) {
            throw new JWTVerificationFailedException("Invalid access token audience.");
        }
        return jwtClaimsSet;
    }

    public JWTClaimsSet verifyAndGetRefreshTokenClaimSet(String token)
            throws JWTVerificationFailedException, ParameterNotFoundException {

        JWTClaimsSet jwtClaimsSet = verifyAndGetClaimSet(token);
        if (!jwtClaimsSet.getIssuer().equals(AUTH_ISSUER)) {
            throw new JWTVerificationFailedException("Invalid refresh token issuer.");
        }

        if (!jwtClaimsSet.getAudience().isEmpty() || !jwtClaimsSet.getAudience().getFirst().equals(AUTH_AUDIENCE)) {
            throw new JWTVerificationFailedException("Invalid refresh token audience.");
        }
        return jwtClaimsSet;
    }

    private JWTClaimsSet verifyAndGetClaimSet(String token)
            throws JWTVerificationFailedException, ParameterNotFoundException {

        try {
            // parse JWT
            SignedJWT signedJWT = SignedJWT.parse(token);

            // load public key PEM
            String pubPem = parameterStoreService.getKey(signedJWT.getHeader().getKeyID());

            // parse into RSAKey (public)
            RSAKey rsaKey = (RSAKey) JWK.parseFromPEMEncodedObjects(pubPem);
            RSAPublicKey publicKey = rsaKey.toRSAPublicKey();

            // verify signature
            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            if (!signedJWT.verify(verifier)) {
                throw new JWTVerificationFailedException("Invalid token signature");
            }
            return signedJWT.getJWTClaimsSet();
        } catch (ParseException | JOSEException e) {
            throw new JWTVerificationFailedException("Failed to verify token signature", e);
        }
    }

}
