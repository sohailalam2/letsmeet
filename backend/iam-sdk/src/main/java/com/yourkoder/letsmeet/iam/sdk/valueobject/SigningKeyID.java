package com.yourkoder.letsmeet.iam.sdk.valueobject;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.yourkoder.letsmeet.iam.sdk.valueobject.exception.InvalidSigningKeyIDException;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

import static java.lang.String.format;

@EqualsAndHashCode
@RegisterForReflection
@JsonDeserialize(builder = SigningKeyID.Builder.class)
public class SigningKeyID implements Serializable {

    @Serial
    private static final long serialVersionUID = -3176648729737900473L;

    @Getter
    private final String value;

    private SigningKeyID(String value) {
        this.value = value;
    }

    public static SigningKeyID fromString(String value) throws InvalidSigningKeyIDException {
        if (value == null) {
            throw new InvalidSigningKeyIDException(
                    "Invalid signing key ID name: [null] provided. Can not be null"
            );
        }

        if (value.isBlank()) {
            throw new InvalidSigningKeyIDException(format(
                    "Invalid signing key ID name: [%s] provided. Can not be blank",
                    value
            ));
        }
        return new SigningKeyID(value);
    }

    @JsonPOJOBuilder
    static class Builder {
        String value;

        Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public SigningKeyID build() throws InvalidSigningKeyIDException {
            return fromString(this.value);
        }
    }
}