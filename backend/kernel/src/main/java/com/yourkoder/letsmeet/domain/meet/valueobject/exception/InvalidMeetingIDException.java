package com.yourkoder.letsmeet.domain.meet.valueobject.exception;

import java.io.Serial;

public class InvalidMeetingIDException extends Exception {
    @Serial
    private static final long serialVersionUID = 4758608922153329386L;

    public InvalidMeetingIDException(String message) {
        super(message);
    }
}
