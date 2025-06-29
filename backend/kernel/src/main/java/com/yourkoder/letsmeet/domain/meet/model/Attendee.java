package com.yourkoder.letsmeet.domain.meet.model;

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
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbUpdateBehavior;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@DynamoDbBean
@RegisterForReflection
public class Attendee {
    private static final String PARTITION_KEY = "ATD";

    @Getter(onMethod = @__({
            @DynamoDbPartitionKey,
            @DynamoDbUpdateBehavior(UpdateBehavior.WRITE_ALWAYS)
    }))
    private String pk = PARTITION_KEY;

    @Getter(onMethod = @__({
            @DynamoDbSortKey,
            @DynamoDbUpdateBehavior(UpdateBehavior.WRITE_ALWAYS)
    }))
    private String sk;

    @Getter(onMethod = @__({@DynamoDbAttribute("attendee_id")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("attendee_id")}))
    private String attendeeId;

    @Getter(onMethod = @__({@DynamoDbAttribute("join_token")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("join_token")}))
    private String joinToken;

    @Getter(onMethod = @__({@DynamoDbAttribute("blocked")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("blocked")}))
    private boolean blocked;

    @DynamoDbIgnore
    public String getMeetingId() {
        return this.sk.split("#")[1];
    }

    @DynamoDbIgnore
    public String getUserId() {
        return this.sk.split("#")[0];
    }

    @DynamoDbIgnore
    public void setPrimaryKey(String userID, String meetingID) {
        this.sk = "%s#%s".formatted(userID, meetingID);
    }

    @DynamoDbIgnore
    public void setSkPrefixFromUserID(String userID) {
        this.sk = userID;
    }
}
