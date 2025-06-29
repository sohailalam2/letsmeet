package com.yourkoder.letsmeet.processmanager.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
@Builder
@RegisterForReflection
public class ChimeMeetingEventDTO {
    private String version;
    private String eventType;
    private String timestamp;
    private String meetingId;
    private String externalMeetingId;
    private String mediaRegion;
}
