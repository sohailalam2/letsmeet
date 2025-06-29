package com.yourkoder.letsmeet.api.meet.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yourkoder.letsmeet.domain.meet.model.MeetingMediaPlacement;
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
public class MeetingMediaPlacementResponseDTO {
    @JsonProperty("audio_host_url")
    private String audioHostUrl;

    @JsonProperty("audio_fallback_url")
    private String audioFallbackUrl;

    @JsonProperty("signaling_url")
    private String signalingUrl;

    @JsonProperty("turn_control_url")
    private String turnControlUrl;

    @JsonProperty("screen_data_url")
    private String screenDataUrl;

    @JsonProperty("screen_viewing_url")
    private String screenViewingUrl;

    @JsonProperty("screen_sharing_url")
    private String screenSharingUrl;

    @JsonProperty("event_ingestion_url")
    private String eventIngestionUrl;

    public static MeetingMediaPlacementResponseDTO from(MeetingMediaPlacement mediaPlacement) {
        return MeetingMediaPlacementResponseDTO.builder()
                .audioHostUrl(mediaPlacement.getAudioHostUrl())
                .audioFallbackUrl(mediaPlacement.getAudioFallbackUrl())
                .signalingUrl(mediaPlacement.getSignalingUrl())
                .turnControlUrl(mediaPlacement.getTurnControlUrl())
                .screenDataUrl(mediaPlacement.getScreenDataUrl())
                .screenViewingUrl(mediaPlacement.getScreenViewingUrl())
                .screenSharingUrl(mediaPlacement.getScreenSharingUrl())
                .eventIngestionUrl(mediaPlacement.getEventIngestionUrl())
                .build();
    }
}
