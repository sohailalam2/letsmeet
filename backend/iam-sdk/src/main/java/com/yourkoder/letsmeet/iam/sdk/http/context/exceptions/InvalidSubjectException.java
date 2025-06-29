package com.yourkoder.letsmeet.iam.sdk.http.context.exceptions;

import java.io.Serial;

public class InvalidSubjectException extends Exception {

    @Serial
    private static final long serialVersionUID = 5254103921655948223L;

    public InvalidSubjectException(String message) {
        super(message);
    }

    public InvalidSubjectException(String message, Throwable cause) {
        super(message, cause);
    }
}
