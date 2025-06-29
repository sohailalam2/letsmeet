package com.yourkoder.letsmeet.domain.meet.service.exception;

import com.yourkoder.letsmeet.domain.meet.error.MeetingErrorCode;
import com.yourkoder.letsmeet.error.ErrorCodedException;

import java.io.Serial;

public class MeetingNotFoundException extends ErrorCodedException {
    @Serial
    private static final long serialVersionUID = 3592387007800677407L;

    public MeetingNotFoundException(String message) {
        super(message);
    }

    public MeetingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getErrorCode() {
        return MeetingErrorCode.RESOURCE_NOT_FOUND;
    }
}
