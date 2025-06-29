package com.yourkoder.letsmeet.api.meet.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yourkoder.letsmeet.domain.meet.valueobject.AttendeeJoinInfo;
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
public class AttendeeJoinResponseDTO {
    @JsonProperty("ready")
    private boolean ready;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("attendee_id")
    private String attendeeId;

    @JsonProperty("join_token")
    private String joinToken;

    @JsonProperty("started_at")
    private Long startedAt;

    public static AttendeeJoinResponseDTO from(AttendeeJoinInfo attendeeJoinInfo) {
        return AttendeeJoinResponseDTO.builder()
                .ready(attendeeJoinInfo.isReady())
                .userId(attendeeJoinInfo.getUserId())
                .attendeeId(attendeeJoinInfo.getAttendeeId())
                .joinToken(attendeeJoinInfo.getJoinToken())
                .startedAt(attendeeJoinInfo.getMeetingStartedAt() != null
                        ? attendeeJoinInfo.getMeetingStartedAt().getEpochSecond() : null)
                .build();
    }
}
