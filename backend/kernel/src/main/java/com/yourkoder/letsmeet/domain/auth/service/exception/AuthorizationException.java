package com.yourkoder.letsmeet.domain.auth.service.exception;

import com.yourkoder.letsmeet.domain.auth.error.AuthErrorCode;
import com.yourkoder.letsmeet.error.ErrorCodedException;

import java.io.Serial;

public class AuthorizationException extends ErrorCodedException {
    @Serial
    private static final long serialVersionUID = -4990377456496900262L;

    public AuthorizationException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return AuthErrorCode.UNAUTHORIZED;
    }
}
