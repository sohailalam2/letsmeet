package com.yourkoder.letsmeet.iam.sdk.service.exception;

import java.io.Serial;

public class JWTVerificationFailedException extends Exception {
    @Serial
    private static final long serialVersionUID = -6432380409656778169L;

    public JWTVerificationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public JWTVerificationFailedException(String message) {
        super(message);
    }
}
