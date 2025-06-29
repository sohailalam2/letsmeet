package com.yourkoder.letsmeet.processmanager.meet.valueobject;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.yourkoder.letsmeet.processmanager.meet.valueobject.exception.InvalidTranscriptException;
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
@JsonDeserialize(builder = Transcript.Builder.class)
public class Transcript implements Serializable {

    @Serial
    private static final long serialVersionUID = 7217173226916427133L;

    private String value;

    private Transcript(String value) {
        this.value = value;
    }

    public static Transcript fromString(String value) throws InvalidTranscriptException {
        if (value == null) {
            throw new InvalidTranscriptException(
                    "Invalid transcript: [null] provided. Can not be null"
            );
        }

        if (value.isBlank()) {
            throw new InvalidTranscriptException(format(
                    "Invalid question transcript: [%s] provided. Can not be blank",
                    value
            ));
        }
        
        return new Transcript(value);
    }

    public Transcript replaceAll(String regex, String replacement) {
        this.value = this.value.replaceAll(regex, replacement);
        return this;
    }

    public Transcript removeIllegals() {
        // Remove blank lines after user ID and lines with empty user ID
        String[] lines = this.value.split("\n");
        StringBuilder filtered = new StringBuilder();
        for (String line : lines) {
            line = line.trim();
            if (!line.matches("<\\d+>:\\s*$") && !line.matches("<>:.*")) {
                filtered.append(line).append("\n");
            }
        }

        // Remove trailing newline
        this.value = filtered.toString().replaceAll("\n$", "");
        return this;
    }

    public Transcript removeDuplicateConsecutives() {
        String[] lines = this.value.split("\n");
        StringBuilder result = new StringBuilder();

        for (String line : lines) {
            // Match consecutive duplicates: word (optional whitespace, optional comma, same word) one or more times
            String cleanedLine = line.replaceAll("(?i)\\b(\\w+)(?:[ \\t]*,?[ \\t]*\\1)+\\b", "$1");
            result.append(cleanedLine).append("\n");
        }

        // Remove trailing newline
        this.value = result.toString().replaceAll("\n$", "");
        return this;
    }

    @JsonPOJOBuilder
    static class Builder {

        String value;

        Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Transcript build() throws InvalidTranscriptException {
            return fromString(this.value);
        }
    }
}
