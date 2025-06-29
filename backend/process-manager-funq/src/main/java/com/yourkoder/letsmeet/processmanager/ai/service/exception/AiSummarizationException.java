package com.yourkoder.letsmeet.processmanager.ai.service.exception;

import com.yourkoder.letsmeet.error.ErrorCodedException;
import com.yourkoder.letsmeet.processmanager.ai.error.AiErrorCode;

import java.io.Serial;

public class AiSummarizationException extends ErrorCodedException {
    @Serial
    private static final long serialVersionUID = 971102536126298316L;

    public AiSummarizationException(String message) {
        super(message);
    }

    public AiSummarizationException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getErrorCode() {
        return AiErrorCode.SUMMARIZATION_FAILED;
    }
}
