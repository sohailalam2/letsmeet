package com.yourkoder.letsmeet.iam.authorizer.token.exception;

import java.io.Serial;

public class JwtClaimsNotApplicableException extends Exception {

    @Serial
    private static final long serialVersionUID = 9118222152048011043L;

    public JwtClaimsNotApplicableException(String message) {
        super(message);
    }
}
