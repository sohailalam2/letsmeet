package com.yourkoder.letsmeet.processmanager.ai.service.exception;

import com.yourkoder.letsmeet.error.ErrorCodedException;
import com.yourkoder.letsmeet.processmanager.ai.error.AiErrorCode;

import java.io.Serial;

public class AiPromptException extends ErrorCodedException {
    @Serial
    private static final long serialVersionUID = 971102536126298316L;

    public AiPromptException(String message) {
        super(message);
    }

    public AiPromptException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getErrorCode() {
        return AiErrorCode.PROMPT_FAILED;
    }
}
