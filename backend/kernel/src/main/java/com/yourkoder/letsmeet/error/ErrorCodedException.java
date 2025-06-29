package com.yourkoder.letsmeet.error;

import java.io.Serial;

public abstract class ErrorCodedException extends Exception {

    @Serial
    private static final long serialVersionUID = -7560093914273732316L;

    public ErrorCodedException() {
        super();
    }

    public ErrorCodedException(String message) {
        super(message);
    }

    public ErrorCodedException(Throwable cause) {
        super(cause);
    }

    public ErrorCodedException(String message, Throwable cause) {
        super(message, cause);
    }

    public final String getErrorCodedMessage() {
        return getFormattedErrorCodedMessage(this.getErrorCode(), this.getMessage());
    }

    public abstract String getErrorCode();

    public static String getFormattedErrorCodedMessage(String errorCode, String message) {
        return "%s: %s".formatted(errorCode, message);
    }
}
