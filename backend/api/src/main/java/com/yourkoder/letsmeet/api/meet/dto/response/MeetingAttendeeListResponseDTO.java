package com.yourkoder.letsmeet.api.meet.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yourkoder.letsmeet.domain.meet.valueobject.UserAttendeeInfo;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
@Getter
@RegisterForReflection
public class MeetingAttendeeListResponseDTO {

    @JsonProperty("id")
    private String meetingID;

    @JsonProperty("attendees")
    private List<UserAttendeeInfo> attendees;
}
