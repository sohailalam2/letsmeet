package com.yourkoder.letsmeet.iam.sdk.valueobject.exception;

import java.io.Serial;

public class InvalidSigningKeyIDException extends Exception {
    @Serial
    private static final long serialVersionUID = 8001274159789889987L;

    public InvalidSigningKeyIDException(String message) {
        super(message);
    }
}
