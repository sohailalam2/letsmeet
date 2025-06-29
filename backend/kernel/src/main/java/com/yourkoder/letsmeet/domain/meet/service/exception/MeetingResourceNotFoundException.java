package com.yourkoder.letsmeet.domain.meet.service.exception;

import com.yourkoder.letsmeet.domain.meet.error.MeetingErrorCode;
import com.yourkoder.letsmeet.error.ErrorCodedException;

import java.io.Serial;

public class MeetingResourceNotFoundException extends ErrorCodedException {

    @Serial
    private static final long serialVersionUID = -685973935087008695L;

    public MeetingResourceNotFoundException(String message) {
        super(message);
    }

    public MeetingResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getErrorCode() {
        return MeetingErrorCode.RESOURCE_NOT_FOUND;
    }
}
