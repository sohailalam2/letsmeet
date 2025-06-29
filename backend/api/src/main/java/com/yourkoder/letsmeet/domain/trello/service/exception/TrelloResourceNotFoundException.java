package com.yourkoder.letsmeet.domain.trello.service.exception;

import com.yourkoder.letsmeet.domain.trello.error.TrelloErrorCode;
import com.yourkoder.letsmeet.error.ErrorCodedException;

import java.io.Serial;

public class TrelloResourceNotFoundException extends ErrorCodedException {
    @Serial
    private static final long serialVersionUID = 7859083455722765933L;

    public TrelloResourceNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return TrelloErrorCode.RESOURCE_NOT_FOUND;
    }
}
