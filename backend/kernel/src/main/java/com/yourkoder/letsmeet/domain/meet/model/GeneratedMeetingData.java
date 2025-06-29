package com.yourkoder.letsmeet.domain.meet.model;

import com.yourkoder.letsmeet.domain.meet.valueobject.ActionItem;
import com.yourkoder.letsmeet.domain.meet.valueobject.MeetingSummary;
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
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbUpdateBehavior;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
@Data
@DynamoDbBean
public class GeneratedMeetingData {
    private static final String PARTITION_KEY = "MTG_SUM";

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

    @Getter(onMethod = @__({@DynamoDbAttribute("summary")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("summary")}))
    private MeetingSummary meetingSummary;

    @Getter(onMethod = @__({@DynamoDbAttribute("action_items")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("action_items")}))
    private List<ActionItem> actionItems;

    @Getter(onMethod = @__({@DynamoDbAttribute("model_id")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("model_id")}))
    private String modelID;

    @Getter(onMethod = @__({@DynamoDbAttribute("processing_time_ms")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("processing_time_ms")}))
    private Long processingTimeMillis;

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

}
