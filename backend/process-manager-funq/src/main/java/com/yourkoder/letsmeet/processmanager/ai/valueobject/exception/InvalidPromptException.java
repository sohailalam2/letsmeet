package com.yourkoder.letsmeet.processmanager.ai.valueobject.exception;

import com.yourkoder.letsmeet.domain.meet.error.MeetingErrorCode;
import com.yourkoder.letsmeet.error.ErrorCodedException;

import java.io.Serial;

public class InvalidPromptException extends ErrorCodedException {

    @Serial
    private static final long serialVersionUID = 4708478112391957239L;

    public InvalidPromptException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return MeetingErrorCode.INVALID_VALUE_OBJECT_DATA;
    }
}
