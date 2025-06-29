package com.yourkoder.letsmeet.util.http;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {

    private static final String UNKNOWN_ERROR_CODE = "UNK-ERR-1111";

    private static final String UNKNOWN_ERROR_OCCURRED_MESSAGE = "Unknown error occurred.";

    @Override
    public Response toResponse(BadRequestException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return getUnkownErrorResponse(UNKNOWN_ERROR_CODE, UNKNOWN_ERROR_OCCURRED_MESSAGE);
        }
        String[] parts = message.trim().split(":", 2);
        if (parts.length == 0) {
            return getUnkownErrorResponse(UNKNOWN_ERROR_CODE, UNKNOWN_ERROR_OCCURRED_MESSAGE);
        }
        if (parts.length != 2) {
            return getUnkownErrorResponse(UNKNOWN_ERROR_CODE, message);
        }
        return getUnkownErrorResponse(parts[0], parts[1]);
    }

    private static Response getUnkownErrorResponse(String errorCode, String message) {
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(ErrorResponse.builder().code(errorCode)
                        .message(message).build())
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    // Inner class to represent the error response
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @RegisterForReflection
    @Builder
    @ToString
    @EqualsAndHashCode
    public static class ErrorResponse {
        private String code;
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}

