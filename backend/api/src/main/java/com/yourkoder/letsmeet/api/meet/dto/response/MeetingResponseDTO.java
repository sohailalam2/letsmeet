package com.yourkoder.letsmeet.api.meet.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yourkoder.letsmeet.domain.meet.valueobject.MeetingInfo;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@ToString
@EqualsAndHashCode
@RegisterForReflection
public class MeetingResponseDTO {
    @JsonProperty("id")
    private String meetingID;

    @JsonProperty("external_meeting_id")
    private String externalMeetingID;

    @JsonProperty("media_region")
    private String mediaRegion;

    @JsonProperty("media_placement")
    private MeetingMediaPlacementResponseDTO mediaPlacement;

    @JsonProperty("host_user_id")
    private String hostUserID;

    @JsonProperty("has_host_joined")
    private boolean hostJoined;

    @JsonProperty("is_host")
    private boolean host;

    @JsonProperty("created_at")
    private Long createdAt;

    @JsonProperty("started_at")
    private Long startedAt;

    @JsonProperty("ended_at")
    private Long endedAt;

    @JsonProperty("is_ended")
    private boolean ended;

    @JsonProperty("waiting_users")
    private Set<String> waitingUsers;

    @JsonProperty("participants")
    private Set<String> participants;

    @JsonProperty("join_url")
    private String meetingJoinURL;

    @JsonProperty("is_summarized")
    private boolean summarized;

    public static MeetingResponseDTO from(MeetingInfo meetingInfo) {
        return MeetingResponseDTO.builder()
                .meetingID(meetingInfo.getMeetingID())
                .externalMeetingID(meetingInfo.getExternalMeetingID())
                .mediaRegion(meetingInfo.getMediaRegion())
                .mediaPlacement(MeetingMediaPlacementResponseDTO.from(meetingInfo.getMediaPlacement()))
                .hostUserID(meetingInfo.getHostUserID())
                .host(meetingInfo.getHost())
                .hostJoined(meetingInfo.isHostJoined())
                .waitingUsers(meetingInfo.getWaitingUsers())
                .participants(meetingInfo.getParticipants())
                .createdAt(meetingInfo.getCreatedAt().getEpochSecond())
                .startedAt(meetingInfo.getStartedAt() != null ? meetingInfo.getStartedAt().getEpochSecond() : null)
                .endedAt(meetingInfo.getEndedAt() != null ? meetingInfo.getEndedAt().getEpochSecond() : null)
                .ended(meetingInfo.isEnded())
                .meetingJoinURL(meetingInfo.getMeetingJoinURL().toString())
                .summarized(meetingInfo.isSummarized())
                .build();
    }
}
