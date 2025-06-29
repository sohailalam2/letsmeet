package com.yourkoder.letsmeet.domain.meet.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.services.chimesdkmeetings.model.MediaPlacement;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
@RegisterForReflection
@EqualsAndHashCode
@ToString
public class MeetingMediaPlacement {

    private String audioHostUrl;

    private String audioFallbackUrl;

    private String signalingUrl;

    private String turnControlUrl;

    private String screenDataUrl;

    private String screenViewingUrl;

    private String screenSharingUrl;

    private String eventIngestionUrl;

    public static MeetingMediaPlacement from(MediaPlacement mediaPlacement) {
        return MeetingMediaPlacement.builder()
                .audioHostUrl(mediaPlacement.audioHostUrl())
                .audioFallbackUrl(mediaPlacement.audioFallbackUrl())
                .signalingUrl(mediaPlacement.signalingUrl())
                .turnControlUrl(mediaPlacement.turnControlUrl())
                .screenDataUrl(mediaPlacement.screenDataUrl())
                .screenViewingUrl(mediaPlacement.screenViewingUrl())
                .screenSharingUrl(mediaPlacement.screenSharingUrl())
                .eventIngestionUrl(mediaPlacement.eventIngestionUrl())
                .build();
    }
}
