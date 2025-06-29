package com.yourkoder.letsmeet.domain.auth.service;

import com.nimbusds.jwt.JWTClaimsSet;
import com.yourkoder.letsmeet.config.ApplicationConfig;
import com.yourkoder.letsmeet.domain.auth.model.User;
import com.yourkoder.letsmeet.domain.auth.repository.UserRepository;
import com.yourkoder.letsmeet.domain.auth.valueobject.UserAuthInfo;
import com.yourkoder.letsmeet.domain.auth.valueobject.UserInfo;
import com.yourkoder.letsmeet.domain.auth.service.exception.AuthorizationException;
import com.yourkoder.letsmeet.iam.sdk.model.WhitelistedToken;
import com.yourkoder.letsmeet.iam.sdk.repository.WhitelistedTokenRepository;
import com.yourkoder.letsmeet.iam.sdk.service.exception.InvalidTokenException;
import com.yourkoder.letsmeet.iam.sdk.service.exception.JWTGenerationFailedException;
import com.yourkoder.letsmeet.iam.sdk.service.exception.JWTVerificationFailedException;
import com.yourkoder.letsmeet.iam.sdk.util.keymanagement.ParameterNotFoundException;
import com.yourkoder.letsmeet.iam.sdk.util.keymanagement.ParameterStoreService;
import com.yourkoder.letsmeet.iam.sdk.valueobject.JwtInfo;
import com.yourkoder.letsmeet.iam.sdk.valueobject.SigningKeyID;
import com.yourkoder.letsmeet.iam.sdk.valueobject.TokenResponse;
import com.yourkoder.letsmeet.iam.sdk.valueobject.exception.InvalidSigningKeyIDException;
import com.yourkoder.letsmeet.util.http.client.google.GoogleTokenClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@JBossLog
@ApplicationScoped
public final class AuthService {

    @Inject
    @RestClient
    GoogleTokenClient tokenClient;

    @Inject
    ApplicationConfig config;

    @Inject
    JwtManagementService jwtService;

    @Inject
    UserRepository userRepository;

    @Inject
    ParameterStoreService parameterStoreService;

    @Inject
    WhitelistedTokenRepository whitelistedTokenRepository;

    private final SigningKeyID privateKey;

    private final SigningKeyID publicKey;

    private final long accessTokenExpiry;

    private final long refreshTokenExpiry;

    public AuthService(ApplicationConfig config) throws InvalidSigningKeyIDException {
        privateKey = SigningKeyID.fromString(config.jws().privateSigningKeyPath());
        publicKey = SigningKeyID.fromString(config.jws().publicSigningKeyPath());

        accessTokenExpiry = config.jws().accessTokenExpiry();
        refreshTokenExpiry = config.jws().refreshTokenExpiry();
    }

    public UserAuthInfo getAuthenticationResponse(
            String code,
            Optional<String> redirectURL
    ) throws JWTGenerationFailedException {
        UserInfo userInfo = getUserInfo(code, redirectURL);

        String userID = userInfo.getUserID();
        Optional<User> userByID = userRepository.findById(userID);

        final User user;
        if (userByID.isEmpty()) {
            user = new User();
            user.initialize(userInfo);
            userRepository.save(user);
        } else {
            user = userByID.get();
            if (userInfo.getPicture() != null) {
                user.setPicture(userInfo.getPicture());
            }
            if (userInfo.getName() != null) {
                user.setName(userInfo.getName());
            }

            if (userInfo.getEmail() != null) {
                user.setEmail(userInfo.getEmail());
            }
            userRepository.updateUser(user);
        }

        // Generate JWT access token
        final JwtInfo accessTokenInfo = jwtService.generateAccessToken(
                user, privateKey, publicKey, accessTokenExpiry);
        final JwtInfo refreshTokenInfo = jwtService.generateRefreshToken(
                userID, privateKey, publicKey, refreshTokenExpiry);

        WhitelistedToken whitelistedAccessToken = new WhitelistedToken();
        whitelistedAccessToken.setPrimaryKey(user.getUserId(), accessTokenInfo.claimsSet().getJWTID());
        Instant now = Instant.now();
        whitelistedAccessToken.setTimestamp(now.getEpochSecond());
        whitelistedAccessToken.setTtl(accessTokenInfo.claimsSet().getExpirationTime().toInstant().getEpochSecond());
        whitelistedTokenRepository.save(whitelistedAccessToken);

        WhitelistedToken whitelistedRefreshToken = new WhitelistedToken();
        whitelistedRefreshToken.setPrimaryKey(user.getUserId(), refreshTokenInfo.claimsSet().getJWTID());
        whitelistedRefreshToken.setTimestamp(now.getEpochSecond());
        whitelistedRefreshToken.setTtl(refreshTokenInfo.claimsSet().getExpirationTime().toInstant().getEpochSecond());
        whitelistedTokenRepository.save(whitelistedRefreshToken);
        // Generate refresh token

        return UserAuthInfo.builder()
                .userID(user.getUserId())
                .accessToken(accessTokenInfo.token())
                .refreshToken(refreshTokenInfo.token())
                .createdAt(user.getCreateAt())
                .build();

    }

    public UserInfo getUserInfo(String code, Optional<String> redirectURL) {
        LOG.debug("Getting token for OIDC token endpoint.");
        // Exchange code for tokens
        if (code == null) {
            throw new RuntimeException("Code not provided.");
        }
        LOG.debugf("Authorization Code: [%s]", code);
        if (config.oidc().clientId() == null) {
            throw new RuntimeException("Client ID not provided.");
        }
        TokenResponse tokenResponse = tokenClient.exchangeCodeForTokens(
                code,
                config.oidc().clientId(),
                config.oidc().clientSecret(),
                redirectURL.orElse(config.oidc().redirectUri()),
                config.oidc().grantType()
        );

        LOG.debug("Retrieved OIDC provider tokens.");
        if (tokenResponse.getAccessToken() == null) {
            LOG.error("Failed to exchange code for tokens");
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        // Fetch user info using access token
        String authorization = "Bearer " + tokenResponse.getAccessToken();

        LOG.debug("Getting user info from OIDC provider.");

        UserInfo userInfo = tokenClient.getUserInfo(authorization);
        LOG.debug("Retrieved user info from OIDC provider.");
        return userInfo;
    }

    public UserAuthInfo refreshUserTokens(String oldRefreshToken) throws JWTGenerationFailedException,
            AuthorizationException, ParameterNotFoundException, JWTVerificationFailedException {
        final JWTClaimsSet oldRefreshTokenJwtClaimsSet;
        oldRefreshTokenJwtClaimsSet = jwtService.verifyAndGetRefreshTokenClaimSet(oldRefreshToken);

        final String userID = oldRefreshTokenJwtClaimsSet.getSubject();

        Optional<User> userOptional = userRepository.findById(userID);
        if (userOptional.isEmpty()) {
            throw new AuthorizationException("Invalid user: [%s]. user does not exist".formatted(userID));
        }
        User user = userOptional.get();

        final JwtInfo accessTokenData = jwtService.generateAccessToken(
                user,
                privateKey,
                publicKey,
                accessTokenExpiry
        );

        final JwtInfo newRefreshTokenData = this.rotateRefreshToken(
                userID,
                oldRefreshTokenJwtClaimsSet
        );

        return UserAuthInfo.builder()
                .userID(user.getUserId())
                .accessToken(accessTokenData.token())
                .refreshToken(newRefreshTokenData.token())
                .createdAt(user.getCreateAt())
                .build();
    }

    private JwtInfo rotateRefreshToken(
            String userID,
            final JWTClaimsSet oldTokenJwtClaimsSet
    ) throws JWTGenerationFailedException {

        JwtInfo newRefreshTokenData = jwtService.generateRefreshToken(
                userID,
                privateKey,
                publicKey,
                refreshTokenExpiry
        );
        try {
            this.replaceToken(
                    newRefreshTokenData.claimsSet(),
                    oldTokenJwtClaimsSet
            );
        } catch (InvalidTokenException e) {
            throw new JWTGenerationFailedException(e);
        }
        return newRefreshTokenData;
    }

    private void replaceToken(
            final JWTClaimsSet newTokenJWTClaimsSet,
            final JWTClaimsSet oldTokenJWTClaimsSet
    ) throws InvalidTokenException {
        final Date newTokenExpirationTime = newTokenJWTClaimsSet.getExpirationTime();

        Instant now = Instant.now();
        if (now.isAfter(newTokenExpirationTime.toInstant())) {
            LOG.debugf("New Token: [%s] to be whitelisted has already expired", newTokenJWTClaimsSet);
            return;
        }

        final String newTokenUserID = newTokenJWTClaimsSet.getSubject();

        final String newTokenJTI = newTokenJWTClaimsSet.getJWTID();

        WhitelistedToken newWhiteListedToken = new WhitelistedToken();
        newWhiteListedToken.setPrimaryKey(newTokenUserID, newTokenJTI);
        newWhiteListedToken.setTimestamp(now.getEpochSecond());
        newWhiteListedToken.setTtl(newTokenExpirationTime.toInstant().getEpochSecond());

        if (now.isAfter(oldTokenJWTClaimsSet.getExpirationTime().toInstant())) {
            LOG.debugf("old Token: [%s] to be whitelisted has already expired", oldTokenJWTClaimsSet);

            whitelistedTokenRepository.save(newWhiteListedToken);
            return;
        }

        final String oldTokenUserID = oldTokenJWTClaimsSet.getSubject();

        if (!oldTokenUserID.equals(newTokenUserID)) {
            throw new InvalidTokenException(
                    "Can not replace whitelist token. tokens belong to different subjects: [%s] and [%s].".formatted(
                    oldTokenUserID,
                    newTokenUserID
            ));
        }

        final String oldTokenJTI = oldTokenJWTClaimsSet.getJWTID();

        WhitelistedToken oldWhiteListedToken = new WhitelistedToken();
        oldWhiteListedToken.setPrimaryKey(oldTokenUserID, oldTokenJTI);

        whitelistedTokenRepository.deleteWhiteListedToken(oldWhiteListedToken);
        whitelistedTokenRepository.save(newWhiteListedToken);
    }

    public void logoutUser(String userID) throws AuthorizationException {
        if (userID == null) {
            throw new AuthorizationException("authorization session is invalid.");
        }
        Optional<User> userOptional = userRepository.findById(userID);
        if (userOptional.isEmpty()) {
            throw new AuthorizationException("Invalid user: [%s]. user does not exist".formatted(userID));
        }
        whitelistedTokenRepository.deleteAllWhitelistedToken(userID);
    }

    public void submitTrelloToken(String userID, String token) throws AuthorizationException {
        userRepository.findById(userID)
                .map(user -> {
                    String keyID = "letsmeet/users/%s/trello/token"
                            .formatted(userID);
                    parameterStoreService.setKey(keyID, token, true);
                    user.setTrelloTokenPath(keyID);
                    userRepository.save(user);
                    return user;
                }).orElseThrow(() -> new AuthorizationException("User unauthorized."));
    }
}
