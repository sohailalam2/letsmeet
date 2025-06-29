package com.yourkoder.letsmeet.domain.meet.valueobject;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@ToString
@EqualsAndHashCode
@RegisterForReflection
public class UserAttendeeInfo {
    @JsonProperty("sub")
    private String userID;

    @JsonProperty("name")
    private String name;

    @JsonProperty("attendee_id")
    private String attendeeID;

    @JsonProperty("picture")
    private String picture;
}
