package com.yourkoder.letsmeet.domain.meet.service.exception;

import com.yourkoder.letsmeet.domain.meet.error.MeetingErrorCode;
import com.yourkoder.letsmeet.error.ErrorCodedException;

import java.io.Serial;

public class FailedToEndMeetingException extends ErrorCodedException {
    @Serial
    private static final long serialVersionUID = 8707301971234337099L;

    public FailedToEndMeetingException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getErrorCode() {
        return MeetingErrorCode.TRANSACTIONAL_STATE_CHANGE_FAILED;
    }
}
