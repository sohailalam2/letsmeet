package com.yourkoder.letsmeet.iam.authorizer.token.valueobject;

import com.nimbusds.jwt.JWTClaimsSet;
import com.yourkoder.letsmeet.iam.authorizer.token.exception.JwtClaimsNotApplicableException;

public enum AuthenticationStatus {
    AUTHENTICATED,
    UNAUTHENTICATED;

    private JWTClaimsSet jwtClaimsSet = null;

    public AuthenticationStatus withClaimSet(JWTClaimsSet jwtClaimsSet) throws JwtClaimsNotApplicableException {
        if (this.equals(UNAUTHENTICATED)) {
            throw new JwtClaimsNotApplicableException("Can not set jwt claims. Authentication has failed.");
        }
        this.jwtClaimsSet = jwtClaimsSet;
        return this;
    }

    public JWTClaimsSet getJwtClaimsSet() throws JwtClaimsNotApplicableException {
        if (this.equals(UNAUTHENTICATED)) {
            throw new JwtClaimsNotApplicableException("Can not get jwt claims. Authentication has failed.");
        }
        return this.jwtClaimsSet;
    }
}
