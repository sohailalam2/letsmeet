package com.yourkoder.letsmeet.domain.meet.valueobject;

import com.yourkoder.letsmeet.domain.meet.model.GeneratedMeetingData;
import com.yourkoder.letsmeet.domain.meet.model.MeetingMediaPlacement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.net.URI;
import java.time.Instant;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@ToString
@EqualsAndHashCode
public class MeetingInfo {
    private String meetingID;
    private String externalMeetingID;
    private String mediaRegion;
    private MeetingMediaPlacement mediaPlacement;
    private String hostUserID;
    private boolean hostJoined;
    private Boolean host;
    private Instant createdAt;
    private Instant startedAt;
    private Instant endedAt;
    private boolean ended;
    private Set<String> waitingUsers;
    private Set<String> participants;
    private URI meetingJoinURL;
    private boolean summarized;
    private GeneratedMeetingData generatedMeetingData;

}
