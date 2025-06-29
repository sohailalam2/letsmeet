package com.yourkoder.letsmeet.domain.meet.service.exception;

import com.yourkoder.letsmeet.domain.meet.error.MeetingErrorCode;
import com.yourkoder.letsmeet.error.ErrorCodedException;

import java.io.Serial;

public class MeetingAttendeeNotFoundException extends ErrorCodedException {
    @Serial
    private static final long serialVersionUID = 3590369827662033164L;

    public MeetingAttendeeNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return MeetingErrorCode.RESOURCE_NOT_FOUND;
    }
}
