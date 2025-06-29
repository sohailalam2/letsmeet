package com.yourkoder.letsmeet.iam.authorizer.token.valueobject.exception;

import java.io.Serial;

public class InvalidJwtFormatException extends Exception {

    @Serial
    private static final long serialVersionUID = -8557577906362165146L;

    public InvalidJwtFormatException(String message) {
        super(message);
    }
}
