package com.yourkoder.letsmeet.processmanager.meet.service.exception;

import com.yourkoder.letsmeet.domain.meet.error.MeetingErrorCode;
import com.yourkoder.letsmeet.error.ErrorCodedException;

import java.io.Serial;

public class InvalidTranscriptSourceException extends ErrorCodedException {
    @Serial
    private static final long serialVersionUID = -4794088060996587413L;

    public InvalidTranscriptSourceException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return MeetingErrorCode.INVALID_VALUE_OBJECT_DATA;
    }
}
