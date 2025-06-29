package com.yourkoder.letsmeet.iam.sdk.http.context.exceptions;

public class NoRequestContextFoundException extends Exception {

    private static final long serialVersionUID = -8260911046447852376L;

    public NoRequestContextFoundException() {
        super("No API request context was found. Can not allow request to complete.");
    }

    public NoRequestContextFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
