package com.yourkoder.letsmeet.domain.auth.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.yourkoder.letsmeet.domain.auth.model.User;
import com.yourkoder.letsmeet.iam.sdk.service.JwtService;
import com.yourkoder.letsmeet.iam.sdk.service.exception.JWTGenerationFailedException;
import com.yourkoder.letsmeet.iam.sdk.util.keymanagement.ParameterNotFoundException;
import com.yourkoder.letsmeet.iam.sdk.valueobject.JwtInfo;
import com.yourkoder.letsmeet.iam.sdk.valueobject.SigningKeyID;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import lombok.extern.jbosslog.JBossLog;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@JBossLog
@ApplicationScoped
@Alternative
@Priority(1)
public class JwtManagementService extends JwtService {

    public JwtInfo generateAccessToken(
            User userInfo,
            SigningKeyID privateKey,
            SigningKeyID publicKey,
            long accessTokenExpiry
    ) throws JWTGenerationFailedException {
        // Build claims
        String jti = UUID.randomUUID().toString();

        Date now = Date.from(Instant.now());
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(userInfo.getUserId())
                .claim("name", userInfo.getName())
                .claim("email", userInfo.getEmail())
                .claim("picture", userInfo.getPicture())
                .issuer(AUTH_ISSUER)
                .audience(API_AUDIENCE)
                .issueTime(now)
                .notBeforeTime(now)                       // nbf
                .expirationTime(Date.from(Instant.now().plusSeconds(accessTokenExpiry)))
                .jwtID(jti)
                .build();

        return new JwtInfo(
                generateJwtToken(privateKey, publicKey, claimsSet),
                claimsSet
        );
    }

    public JwtInfo generateRefreshToken(
            String userID,
            SigningKeyID privateKey,
            SigningKeyID publicKey,
            long refreshTokenExpiry
    ) throws JWTGenerationFailedException {
        // current time
        Date now = Date.from(Instant.now());

        // build a cryptographically‑random JWT ID for rotation/revocation tracking
        String jti = UUID.randomUUID().toString();

        JWTClaimsSet refreshClaims = new JWTClaimsSet.Builder()
                .subject(userID)              // internal user identifier only
                .issuer(AUTH_ISSUER)
                .audience(AUTH_AUDIENCE)
                .issueTime(now)                           // iat
                .notBeforeTime(now)                       // nbf
                .expirationTime(Date.from(Instant.now().plusSeconds(refreshTokenExpiry)))  // exp
                .jwtID(jti)                               // jti
                .build();

        return new JwtInfo(
                generateJwtToken(privateKey, publicKey, refreshClaims),
                refreshClaims
        );
    }

    private String generateJwtToken(
            SigningKeyID privateKey,
            SigningKeyID publicKey,
            JWTClaimsSet claimsSet
    ) throws JWTGenerationFailedException {
        try {
            // Load key strings from Parameter Store
            String privateKeyPem = parameterStoreService.getKey(privateKey.getValue());

            // Convert PEM string to RSAPrivateKey
            RSAKey rsaKey = (RSAKey) JWK.parseFromPEMEncodedObjects(privateKeyPem);
            assert rsaKey.isPrivate();
            RSASSASigner signer = new RSASSASigner(rsaKey.toPrivateKey());

            // Build header (optional: use `rsaKey.getKeyID()` if available)
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(publicKey.getValue())
                    .build();

            // Sign the JWT
            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (ParameterNotFoundException | JOSEException e) {
            throw new JWTGenerationFailedException(e);
        }
    }

}
