package com.yourkoder.letsmeet.domain.meet.valueobject;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.yourkoder.letsmeet.domain.meet.valueobject.exception.InvalidMeetingIDException;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

import static java.lang.String.format;

@EqualsAndHashCode
@RegisterForReflection
@JsonDeserialize(builder = MeetingID.Builder.class)
public class MeetingID implements Serializable {

    @Serial
    private static final long serialVersionUID = -1515739425948097201L;
    
    @Getter
    private final String value;

    private MeetingID(String value) {
        this.value = value;
    }

    public static MeetingID fromString(String value) throws InvalidMeetingIDException {
        if (value == null) {
            throw new InvalidMeetingIDException(
                    "Invalid signing key ID name: [null] provided. Can not be null"
            );
        }

        if (value.isBlank()) {
            throw new InvalidMeetingIDException(format(
                    "Invalid signing key ID name: [%s] provided. Can not be blank",
                    value
            ));
        }
        return new MeetingID(value);
    }

    @JsonPOJOBuilder
    static class Builder {
        String value;

        Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public MeetingID build() throws InvalidMeetingIDException {
            return fromString(this.value);
        }
    }
}