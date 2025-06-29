package com.yourkoder.letsmeet.domain.meet.model;

import com.yourkoder.letsmeet.util.database.SetToListAttributeConverter;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.UpdateBehavior;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbUpdateBehavior;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@DynamoDbBean
@RegisterForReflection
public class Meeting {
    private static final String PARTITION_KEY = "MTG";

    @Getter(onMethod = @__({
            @DynamoDbPartitionKey,
            @DynamoDbUpdateBehavior(UpdateBehavior.WRITE_ALWAYS)
    }))
    @Builder.Default
    private String pk = PARTITION_KEY;

    @Getter(onMethod = @__({
            @DynamoDbSortKey,
            @DynamoDbUpdateBehavior(UpdateBehavior.WRITE_ALWAYS)
    }))
    private String sk;

    @Getter(onMethod = @__({@DynamoDbAttribute("meeting_arn")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("meeting_arn")}))
    private String meetingArn;

    @Getter(onMethod = @__({@DynamoDbAttribute("external_meeting_id")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("external_meeting_id")}))
    private String externalMeetingId;

    @Getter(onMethod = @__({@DynamoDbAttribute("media_region")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("media_region")}))
    private String mediaRegion;

    @Getter(onMethod = @__({@DynamoDbAttribute("host")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("host")}))
    private String hostUserId;

    @Getter(onMethod = @__({@DynamoDbAttribute("created_at")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("created_at")}))
    private Long createdAtEpochSec;

    @Getter(onMethod = @__({@DynamoDbAttribute("started_at")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("started_at")}))
    private Long startedAtEpochSec;

    @Getter(onMethod = @__({@DynamoDbAttribute("ended_at")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("ended_at")}))
    private Long endedAtEpochSec;

    @Getter(onMethod = @__({@DynamoDbAttribute("host_joined")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("host_joined")}))
    private boolean hostJoined;

    @Getter(onMethod = @__({@DynamoDbAttribute("ended")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("ended")}))
    private boolean ended;

    @Getter(onMethod = @__({@DynamoDbAttribute("capture_pipeline_id")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("capture_pipeline_id")}))
    private String capturePipelineId;

    @Getter(onMethod = @__({@DynamoDbAttribute("capture_pipeline_arn")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("capture_pipeline_arn")}))
    private String capturePipelineArn;

    @Getter(onMethod = @__({@DynamoDbAttribute("capture_pipeline_complete")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("capture_pipeline_complete")}))
    private boolean capturePipelineComplete;

    @Getter(onMethod = @__({@DynamoDbAttribute("concatenation_pipeline_id")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("concatenation_pipeline_id")}))
    private String concatenationPipelineId;

    @Getter(onMethod = @__({@DynamoDbAttribute("concatenation_pipeline_arn")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("concatenation_pipeline_arn")}))
    private String concatenationPipelineArn;

    @Getter(onMethod = @__({@DynamoDbAttribute("concatenation_pipeline_status")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("concatenation_pipeline_status")}))
    private String concatenationPipelineStatus;

    @Getter(onMethod = @__({@DynamoDbAttribute("transcribing")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("transcribing")}))
    private boolean transcribing;

    @Getter(onMethod = @__({@DynamoDbAttribute("media_placement")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("media_placement")}))
    private MeetingMediaPlacement mediaPlacement;

    @Getter(onMethod = @__({@DynamoDbAttribute("video_recording")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("video_recording")}))
    private String videoRecording;

    @Getter(onMethod = @__({
            @DynamoDbAttribute("waiting"),
            @DynamoDbConvertedBy(SetToListAttributeConverter.class)
    }))
    @Setter(onMethod = @__({
            @DynamoDbAttribute("waiting"),
            @DynamoDbConvertedBy(SetToListAttributeConverter.class)
    }))
    @Builder.Default
    private Set<String> waitingUsers = new HashSet<>();

    @Getter(onMethod = @__({
            @DynamoDbAttribute("participants"),
            @DynamoDbConvertedBy(SetToListAttributeConverter.class)
    }))
    @Setter(onMethod = @__({
            @DynamoDbAttribute("participants"),
            @DynamoDbConvertedBy(SetToListAttributeConverter.class)
    }))
    @Builder.Default
    private Set<String> participantIds = new HashSet<>();

    @Getter(onMethod = @__({
            @DynamoDbAttribute("bocked"),
            @DynamoDbConvertedBy(SetToListAttributeConverter.class)
    }))
    @Setter(onMethod = @__({
            @DynamoDbAttribute("bocked"),
            @DynamoDbConvertedBy(SetToListAttributeConverter.class)
    }))
    @Builder.Default
    private Set<String> blockedIds = new HashSet<>();

    @Getter(onMethod = @__({@DynamoDbAttribute("summarized")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("summarized")}))
    private boolean summarized;

    @DynamoDbIgnore
    public String getMeetingId() {
        return this.sk;
    }

    @DynamoDbIgnore
    public void setSkFromMeetingId(String meetingID) {
        this.sk = meetingID;
    }

    @DynamoDbIgnore
    public void setPrimaryKey(String meetingID) {
        this.setSkFromMeetingId(meetingID);
    }

    @DynamoDbIgnore
    public void initialize(
            String hostUserId,
            String meetingId,
            String meetingArn,
            String externalMeetingId,
            String mediaRegion,
            MeetingMediaPlacement meetingMediaPlacement
    ) {
        this.setPrimaryKey(meetingId);
        this.setMeetingArn(meetingArn);
        this.setExternalMeetingId(externalMeetingId);
        this.setMediaRegion(mediaRegion);
        this.setHostUserId(hostUserId);
        this.setHostJoined(false);
        this.setEnded(false);
        this.setCreatedAt(Instant.now());
        this.setMediaPlacement(meetingMediaPlacement);
    }

    @DynamoDbIgnore
    public boolean isHost(String userId) {
        return this.hostUserId != null && this.hostUserId.equals(userId);
    }

    @DynamoDbIgnore
    public Instant getCreatedAt() {
        return Instant.ofEpochSecond(this.createdAtEpochSec);
    }

    @DynamoDbIgnore
    public void setCreatedAt(Instant createdAt) {
        this.createdAtEpochSec = createdAt.getEpochSecond();
    }

    @DynamoDbIgnore
    public Instant getStartedAt() {
        if (this.startedAtEpochSec == null) {
            return null;
        }
        return Instant.ofEpochSecond(this.startedAtEpochSec);
    }

    @DynamoDbIgnore
    public void setStartedAt(Instant startedAt) {
        this.startedAtEpochSec = startedAt.getEpochSecond();
    }

    @DynamoDbIgnore
    public Instant getEndedAt() {
        if (this.endedAtEpochSec == null) {
            return null;
        }
        return Instant.ofEpochSecond(this.endedAtEpochSec);
    }

    @DynamoDbIgnore
    public void setEndedAt(Instant endedAt) {
        this.endedAtEpochSec = endedAt.getEpochSecond();
    }
}
