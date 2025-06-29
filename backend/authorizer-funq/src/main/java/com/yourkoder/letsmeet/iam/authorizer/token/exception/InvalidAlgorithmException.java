package com.yourkoder.letsmeet.iam.authorizer.token.exception;

import java.io.Serial;

public class InvalidAlgorithmException extends Exception {

    @Serial
    private static final long serialVersionUID = 4866096224588883130L;

    public InvalidAlgorithmException(String message) {
        super(message);
    }
}
