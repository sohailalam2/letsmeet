package com.yourkoder.letsmeet.api.meet.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
@Getter
@RegisterForReflection
public class MeetingVideoInfoDTO {

    @JsonProperty("id")
    private String meetingID;

    @JsonProperty("signed_url")
    private final String signedURL;
}
