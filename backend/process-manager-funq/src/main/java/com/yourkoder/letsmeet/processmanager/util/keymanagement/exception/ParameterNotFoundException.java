package com.yourkoder.letsmeet.processmanager.util.keymanagement.exception;

import java.io.Serial;

public class ParameterNotFoundException extends Exception {
    @Serial
    private static final long serialVersionUID = 3289694871718149370L;

    public ParameterNotFoundException(String message) {
        super(message);
    }
}
