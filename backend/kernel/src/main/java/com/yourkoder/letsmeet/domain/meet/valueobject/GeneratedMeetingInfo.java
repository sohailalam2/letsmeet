package com.yourkoder.letsmeet.domain.meet.valueobject;

import com.yourkoder.letsmeet.domain.auth.model.User;
import com.yourkoder.letsmeet.domain.meet.model.GeneratedMeetingData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@ToString
@EqualsAndHashCode
public class GeneratedMeetingInfo {
    private String meetingID;
    private Map<String, User> users;
    private GeneratedMeetingData generatedMeetingData;

}
