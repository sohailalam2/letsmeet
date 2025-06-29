package com.yourkoder.letsmeet.processmanager.ai.valueobject;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.yourkoder.letsmeet.processmanager.ai.valueobject.exception.InvalidPromptException;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

import static java.lang.String.format;

@Getter
@EqualsAndHashCode
@ToString
@RegisterForReflection
@JsonDeserialize(builder = Prompt.Builder.class)
public class Prompt implements Serializable {

    @Serial
    private static final long serialVersionUID = 1800640301265997991L;
    
    private String value;

    private Prompt(String value) {
        this.value = value;
    }

    public static Prompt fromString(String value) throws InvalidPromptException {
        if (value == null) {
            throw new InvalidPromptException(
                    "Invalid prompt: [null] provided. Can not be null"
            );
        }

        if (value.isBlank()) {
            throw new InvalidPromptException(format(
                    "Invalid prompt: [%s] provided. Can not be blank",
                    value
            ));
        }
        
        return new Prompt(value);
    }

    @JsonPOJOBuilder
    static class Builder {

        String value;

        Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Prompt build() throws InvalidPromptException {
            return fromString(this.value);
        }
    }
}
