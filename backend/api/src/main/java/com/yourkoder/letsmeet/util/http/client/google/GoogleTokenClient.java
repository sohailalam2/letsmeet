package com.yourkoder.letsmeet.util.http.client.google;

import com.yourkoder.letsmeet.iam.sdk.valueobject.TokenResponse;
import com.yourkoder.letsmeet.domain.auth.valueobject.UserInfo;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(baseUri = "https://www.googleapis.com/oauth2")
public interface GoogleTokenClient {

    @POST
    @Path("/v4/token")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    TokenResponse exchangeCodeForTokens(
            @FormParam("code") String code,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("redirect_uri") String redirectUri,
            @FormParam("grant_type") String grantType
    );

    @GET
    @Path("/v3/userinfo")
    @Produces(MediaType.APPLICATION_JSON)
    UserInfo getUserInfo(@HeaderParam("Authorization") String authorization);
}