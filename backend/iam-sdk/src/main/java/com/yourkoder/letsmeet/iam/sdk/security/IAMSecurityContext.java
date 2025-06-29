package com.yourkoder.letsmeet.iam.sdk.security;

import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

public class IAMSecurityContext implements SecurityContext {

    private final UriInfo uriInfo;

    private final Principal principal;

    @Getter
    private final Map<String, String> claims;

    public IAMSecurityContext(
            UriInfo uriInfo,
            String subject,
            Map<String, String> claims
    ) {
        this.uriInfo = uriInfo;
        this.principal = new JwtUserPrincipal(subject);
        this.claims = new HashMap<>(claims);
    }

    @Override
    public Principal getUserPrincipal() {
        return this.principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return true;
    }

    @Override
    public boolean isSecure() {
        return uriInfo.getAbsolutePath().toString().startsWith("https");
    }

    @Override
    public String getAuthenticationScheme() {
        return "JWT";
    }

    @AllArgsConstructor
    private static class JwtUserPrincipal implements Principal {

        private String upn;

        @Override
        public String getName() {
            return this.upn;
        }

        @Override
        public boolean implies(Subject subject) {
            return Principal.super.implies(subject);
        }
    }
}
