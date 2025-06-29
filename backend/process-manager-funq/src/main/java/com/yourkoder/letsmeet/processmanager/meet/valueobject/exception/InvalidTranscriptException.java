package com.yourkoder.letsmeet.processmanager.meet.valueobject.exception;

import com.yourkoder.letsmeet.domain.meet.error.MeetingErrorCode;
import com.yourkoder.letsmeet.error.ErrorCodedException;

import java.io.Serial;

public class InvalidTranscriptException extends ErrorCodedException {

    @Serial
    private static final long serialVersionUID = 4708478112391957239L;

    public InvalidTranscriptException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return MeetingErrorCode.INVALID_VALUE_OBJECT_DATA;
    }
}
