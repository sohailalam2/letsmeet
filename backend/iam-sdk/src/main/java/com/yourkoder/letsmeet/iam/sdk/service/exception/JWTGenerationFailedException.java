package com.yourkoder.letsmeet.iam.sdk.service.exception;

import java.io.Serial;

public class JWTGenerationFailedException extends Exception {
    @Serial
    private static final long serialVersionUID = 5728039149720847051L;

    public JWTGenerationFailedException(Throwable cause) {
        super(cause);
    }
}
