package com.yourkoder.letsmeet.domain.meet.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@ToString
@EqualsAndHashCode
public class AttendeeJoinInfo {
    private boolean ready;

    private String userId;

    private String attendeeId;

    private String joinToken;

    private Instant meetingStartedAt;

}
