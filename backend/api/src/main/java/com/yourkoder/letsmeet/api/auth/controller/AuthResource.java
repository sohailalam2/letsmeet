package com.yourkoder.letsmeet.api.auth.controller;

import com.yourkoder.letsmeet.api.auth.dto.request.TrelloTokenSubmissionRequestDTO;
import com.yourkoder.letsmeet.api.auth.dto.response.UserAuthResponseDTO;
import com.yourkoder.letsmeet.config.ApplicationConfig;
import com.yourkoder.letsmeet.domain.auth.service.AuthService;
import com.yourkoder.letsmeet.domain.auth.service.exception.AuthorizationException;
import com.yourkoder.letsmeet.domain.auth.valueobject.OIDCQueryParams;
import com.yourkoder.letsmeet.domain.auth.valueobject.UserAuthInfo;
import com.yourkoder.letsmeet.iam.sdk.service.exception.JWTGenerationFailedException;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriBuilder;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import java.util.Optional;

@JBossLog
@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Context
    SecurityContext securityContext;

    @Inject
    AuthService authService;

    @Inject
    ApplicationConfig googleConfig;

    @GET
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login() {
        // Return user info as response

        UriBuilder uriBuilder = UriBuilder.fromUri(googleConfig.oidc().authorization())
                .queryParam(OIDCQueryParams.RESPONSE_TYPE, "code")
                .queryParam(OIDCQueryParams.CLIENT_ID, googleConfig.oidc().clientId())
                .queryParam(OIDCQueryParams.SCOPE, String.join(" ", googleConfig.oidc().scope()))
                .queryParam(OIDCQueryParams.REDIRECT_URI, googleConfig.oidc().redirectUri())
                .queryParam(OIDCQueryParams.STATE, "abc124");
        return Response.temporaryRedirect(uriBuilder.build()).build();
    }

    @GET
    @Path("/code")
    @Operation(
            operationId = "submitAuthCode",
            summary = "post the authorisation code",
            description = """
                    Post the IAM authorization code for SkillsFactory to exchange it and get the ID token for the
                    user and sign them up or log them in. When signed up successfully, the user needs to login again
                    to create an authenticated session."""
    )
    @APIResponses(
            {
                    @APIResponse(
                            responseCode = "200",
                            description = """
                        The user logged in successfully.
                        The user gets a session cookie after logging in.""",
                            content = @Content(
                                    schema = @Schema(implementation = UserAuthResponseDTO.class)
                            )
                    ),
                    @APIResponse(
                            responseCode = "400",
                            description = """
                        The user signed using bad organisation ID or there was a failure during authentication
                        with the IAM provider.."""
                    ),
                    @APIResponse(
                            responseCode = "500",
                            description = """
                            The user could not be authenticated due to internal application
                            or infrastructure issues."""
                    ),
            }
    )
    @Produces(MediaType.APPLICATION_JSON)
    public UserAuthResponseDTO submitAuthCode(
            @QueryParam("code") String code,
            @QueryParam("redirect_uri") String redirectURL
    ) {
        if (code == null || code.isEmpty()) {
            LOG.error("Authorization code is required");
            throw new BadRequestException();
        }

        try {
            UserAuthInfo userAuthInfo = authService.getAuthenticationResponse(code, Optional.ofNullable(redirectURL));
            // Return tokens in response
            return UserAuthResponseDTO.builder()
                    .userID(userAuthInfo.getUserID())
                    .accessToken(userAuthInfo.getAccessToken())
                    .refreshToken(userAuthInfo.getRefreshToken())
                    .createdAt(userAuthInfo.getCreatedAt().getEpochSecond())
                    .build();
        } catch (JWTGenerationFailedException e) {
            LOG.error(e.getMessage(), e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("/trello/token")
    @Operation(
            operationId = "submitTrelloToken",
            summary = "post the trello token of a user"
    )
    @APIResponses(
            {
                    @APIResponse(
                            responseCode = "204",
                            description = """
                            The user integrated trello successfully."""
                    ),
                    @APIResponse(
                            responseCode = "400",
                            description = """
                        The user was unauthorized."""
                    ),
                    @APIResponse(
                            responseCode = "500",
                            description = """
                            The user could not be authenticated due to internal application
                            or infrastructure issues."""
                    ),
            }
    )
    @Consumes(MediaType.APPLICATION_JSON)
    public void submitTrelloToken(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            TrelloTokenSubmissionRequestDTO requestDTO
    ) {
        if (authorization == null || securityContext.getUserPrincipal() == null
                || securityContext.getUserPrincipal().getName().equalsIgnoreCase("Anonymous")) {
            throw new BadRequestException();
        }
        try {
            String userID = securityContext.getUserPrincipal().getName();
            authService.submitTrelloToken(userID, requestDTO.getToken());
        } catch (AuthorizationException e) {
            LOG.error(e.getMessage(), e);
            throw new BadRequestException(e.getErrorCodedMessage());
        }
    }

    @GET
    @Path("/logout")
    @Operation(
            operationId = "logoutUser",
            summary = "Log out a logged in user by removing its session."
    )
    @APIResponses(
            {
                    @APIResponse(
                            responseCode = "204",
                            description = """
                                    The user's session was removed and the browser
                                    session cookie will also be removed."""
                    ),
                    @APIResponse(
                            responseCode = "400",
                            description = "The user requested to logout without logging in."
                    ),
                    @APIResponse(
                            responseCode = "500",
                            description = "The user requested to logout but the http cookie was malformed."
                    ),
            }
    )
    public void logoutUser(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization) {
        if (authorization == null || securityContext.getUserPrincipal() == null
                || securityContext.getUserPrincipal().getName().equalsIgnoreCase("Anonymous")) {
            throw new BadRequestException();
        }
        try {
            String userID = securityContext.getUserPrincipal().getName();
            authService.logoutUser(userID);
        } catch (AuthorizationException e) {
            LOG.error(e.getMessage(), e);
            throw new BadRequestException(e.getErrorCodedMessage());
        }
    }
}
