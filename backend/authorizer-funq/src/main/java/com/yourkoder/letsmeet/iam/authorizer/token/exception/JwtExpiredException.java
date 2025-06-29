package com.yourkoder.letsmeet.iam.authorizer.token.exception;

import java.io.Serial;

public class JwtExpiredException extends Exception {

    @Serial
    private static final long serialVersionUID = -2461350679918547613L;

    public JwtExpiredException(String message) {
        super(message);
    }
}
