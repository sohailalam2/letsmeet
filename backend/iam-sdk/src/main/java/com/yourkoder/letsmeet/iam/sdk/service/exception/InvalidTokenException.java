package com.yourkoder.letsmeet.iam.sdk.service.exception;

import java.io.Serial;

public class InvalidTokenException extends Exception {
    @Serial
    private static final long serialVersionUID = -283061777091491431L;

    public InvalidTokenException(String message) {
        super(message);
    }
}
