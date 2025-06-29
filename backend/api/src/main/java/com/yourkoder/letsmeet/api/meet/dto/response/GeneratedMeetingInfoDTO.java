package com.yourkoder.letsmeet.api.meet.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yourkoder.letsmeet.domain.auth.model.User;
import com.yourkoder.letsmeet.domain.meet.model.GeneratedMeetingData;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
@Getter
@RegisterForReflection
public class GeneratedMeetingInfoDTO {

    @JsonProperty("id")
    private String meetingID;

    @JsonProperty("summary")
    private final MeetingSummaryDTO summary;

    @JsonProperty("action_items")
    private final List<ActionItemDTO> actionItems;

    public static GeneratedMeetingInfoDTO from(GeneratedMeetingData meetingInfo, Map<String, User> users) {
        return GeneratedMeetingInfoDTO.builder()
                .meetingID(meetingInfo.getMeetingId())
                .summary(meetingInfo.getMeetingSummary() != null
                        ? MeetingSummaryDTO.from(meetingInfo.getMeetingSummary()) : null)
                .actionItems(meetingInfo.getActionItems() != null
                        ? meetingInfo.getActionItems().stream()
                                .map(actionItem -> ActionItemDTO.from(
                                        actionItem,
                                        users.get(actionItem.getAssignee())
                                ))
                                .toList()
                        : new ArrayList<>())
                .build();
    }
}
